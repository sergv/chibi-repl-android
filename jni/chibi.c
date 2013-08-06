
#include <android_chibi_scheme_repl_Native.h>

#include <jni.h>
#include <stdlib.h>
#include <unistd.h>

#include <chibi/eval.h>
#include <chibi/sexp.h>


#define INITIAL_HEAP_SIZE 512 * 1024
#define MAXIMUM_HEAP_SIZE 8 * 1024 * 1024

#ifndef ANDROID_LOG_TAG
#define ANDROID_LOG_TAG "chibi"
#endif

#define JNI_FN(F) Java_android_chibi_scheme_repl_ChibiInterpreter_ ## F

#undef NDEBUG

#ifndef NDEBUG
#include <android/log.h>

#define DEBUG(args...) \
    __android_log_print(ANDROID_LOG_DEBUG, ANDROID_LOG_TAG, args)

#define ERROR(args...) \
    __android_log_print(ANDROID_LOG_ERROR, ANDROID_LOG_TAG, args)

#define INFO(args...) \
    __android_log_print(ANDROID_LOG_INFO, ANDROID_LOG_TAG, args)

#else

#define DEBUG(args...)
#define ERROR(args...)
#define INFO(args...)

#endif /* #ifndef NDEBUG */


/* static char const * const runtime_exception = "java/lang/RuntimeException";
#define RUNTIME_EXCEPTION runtime_exception */


typedef struct interpreter_state {
        sexp ctx;
        sexp in;
        sexp err;
        sexp out;
} interpreter_state_t;


JNI_OnLoad(JavaVM *jvm, void *reserved)
{
        DEBUG("initializing chibi.so");
        sexp_scheme_init();
        return JNI_VERSION_1_2;
}

static void setup_string_ports(interpreter_state_t *state)
{
        /* this is a customized version of sexp_load_standard_ports */
        sexp_gc_var2(in, out);
        /* intentionally no input */
        in = sexp_open_input_string(state->ctx,
                                    sexp_c_string(state->ctx, "", -1));
        out = sexp_open_output_string(state->ctx);

        sexp_gc_preserve2(state->ctx, in, out);
        /* do not close */
        sexp_port_no_closep(in) = 1;
        sexp_port_no_closep(out) = 1;

        sexp_set_parameter(state->ctx,
                           NULL,
                           sexp_global(state->ctx, SEXP_G_CUR_IN_SYMBOL), in);
        sexp_set_parameter(state->ctx,
                           NULL,
                           sexp_global(state->ctx, SEXP_G_CUR_OUT_SYMBOL), out);
        sexp_set_parameter(state->ctx,
                           NULL,
                           sexp_global(state->ctx, SEXP_G_CUR_ERR_SYMBOL), out);

        state->in = in;
        state->err = out;
        state->out = out;
        sexp_preserve_object(state->ctx, state->in);
        sexp_preserve_object(state->ctx, state->out);
        sexp_gc_release2(state->ctx);
}

static void close_string_ports(interpreter_state_t *state)
{
        sexp_close_port(state->ctx, state->in);
        /* err is the same as out for now, so don't close it */
        sexp_close_port(state->ctx, state->out);
        sexp_release_object(state->ctx, state->in);
        sexp_release_object(state->ctx, state->out);
}

JNIEXPORT jlong JNICALL
JNI_FN(init)
(JNIEnv *env, jclass clazz, jstring working_dir)
{
        /* precondition: working_dir is populated with files available for
           loading */
        /* todo: make some initialization code, ensure
           (import (scheme base)) is executed */
        /* todo: ensure threads are loaded */
        interpreter_state_t *state = NULL;
        char const *dir = NULL;

        state = calloc(1, sizeof(interpreter_state_t));
        if (!state) {
                goto cleanup;
        }

        dir = (*env)->GetStringUTFChars(env, working_dir, NULL);

        state->ctx = sexp_make_eval_context(NULL /* ctx */,
                                            NULL /* stack */,
                                            NULL /* env */,
                                            INITIAL_HEAP_SIZE /* size */,
                                            MAXIMUM_HEAP_SIZE);
        /* may be handy
           chdir(dir); */
        sexp_add_module_directory(state->ctx,
                                  sexp_c_string(state->ctx, dir, -1),
                                  SEXP_TRUE);

        sexp_load_standard_env(state->ctx, NULL, SEXP_SEVEN);
        setup_string_ports(state);

        (*env)->ReleaseStringUTFChars(env, working_dir, dir);
cleanup:
        return (jlong)state;
}

JNIEXPORT void JNICALL
JNI_FN(release)
(JNIEnv *env, jclass clazz, jlong interpreter_handle)
{
        interpreter_state_t *state = (interpreter_state_t *) interpreter_handle;
        if (state) {
                close_string_ports(state);
                sexp_destroy_context(state->ctx);
                free(state);
        }
}

JNIEXPORT void JNICALL
JNI_FN(eval)
(JNIEnv *env, jclass clazz, jlong interpreter_handle, jstring code,
 jobjectArray out_result, jbooleanArray out_error)
{
        interpreter_state_t *state = (interpreter_state_t *) interpreter_handle;
        jstring result = NULL;
        jboolean had_error = JNI_FALSE;
        char const *s = NULL;
        s = (*env)->GetStringUTFChars(env, code, NULL);

        sexp_gc_var5(obj, res, bindings, out_str, out);
        sexp_gc_preserve5(state->ctx, obj, res, bindings, out_str, out);

        out = sexp_open_output_string(state->ctx);
        sexp_set_parameter(state->ctx, NULL, sexp_global(state->ctx, SEXP_G_CUR_ERR_SYMBOL), out);
        sexp_set_parameter(state->ctx, NULL, sexp_global(state->ctx, SEXP_G_CUR_OUT_SYMBOL), out);

        obj = sexp_read_from_string(state->ctx, s, -1);

        if (sexp_exceptionp(obj)) {
                sexp_print_exception(state->ctx, obj, out);
                had_error = JNI_TRUE;
        } else {
                sexp_context_top(state->ctx) = 0;
                if (!(sexp_idp(obj) || sexp_pairp(obj) || sexp_nullp(obj))) {
                        obj = sexp_make_lit(state->ctx, obj);
                }
#if SEXP_USE_WARN_UNDEFS
                bindings = sexp_env_bindings(sexp_context_env(state->ctx));
#endif
                res = sexp_eval(state->ctx, obj, NULL);
#if SEXP_USE_WARN_UNDEFS
                sexp_warn_undefs(state->ctx,
                                 sexp_env_bindings(sexp_context_env(state->ctx)),
                                 bindings,
                                 res);
#endif

                if (res && sexp_exceptionp(res)) {
                        sexp_print_exception(state->ctx, res, out);
                        sexp_stack_trace(state->ctx, out);
                        had_error = JNI_TRUE;
                } else if (res != SEXP_VOID) {
                        sexp_write(state->ctx, res, out);
                        sexp_newline(state->ctx, out);
                }
        }
        out_str = sexp_get_output_string(state->ctx, out);

        sexp_set_parameter(state->ctx, NULL, sexp_global(state->ctx, SEXP_G_CUR_ERR_SYMBOL), state->err);
        sexp_set_parameter(state->ctx, NULL, sexp_global(state->ctx, SEXP_G_CUR_OUT_SYMBOL), state->out);

        result = (*env)->NewStringUTF(env, sexp_string_data(out_str));

        (*env)->ReleaseStringUTFChars(env, code, s);
        sexp_gc_release5(state->ctx);

        (*env)->SetObjectArrayElement(env, out_result, 0, result);
        (*env)->SetBooleanArrayRegion(env, out_error, 0, 1, &had_error);
}

