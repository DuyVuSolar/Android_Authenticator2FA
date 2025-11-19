package com.kuemiin.reversevoice.utils.glide;

import android.content.Context;

import androidx.annotation.NonNull;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

@GlideModule
public class ProgressAppGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565));
    }
//
//    @Override
//    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
//        super.registerComponents(context, glide, registry);
//        OkHttpClient.Builder client = new OkHttpClient.Builder()
//                .addNetworkInterceptor(chain -> {
//                    Request request = chain.request();
//                    Response response = chain.proceed(request);
//                    ResponseProgressListener listener = new DispatchingProgressListener();
//                    return response.newBuilder()
//                            .body(new OkHttpProgressResponseBody(request.url(), response.body(), listener))
//                            .build();
//                })
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(300, TimeUnit.SECONDS);
//        NetworkUtils.Companion.unsafeOkHttpClient(client);
//        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client.build()));
//    }
//
//    public static void forget(String url) {
//        DispatchingProgressListener.forget(url);
//    }
//    public static void expect(String url, UIonProgressListener listener) {
//        DispatchingProgressListener.expect(url, listener);
//    }
//
//    private interface ResponseProgressListener {
//        void update(HttpUrl url, long bytesRead, long contentLength);
//    }
//
//    public interface UIonProgressListener {
//        void onProgress(long bytesRead, long expectedLength);
//        /**
//         * Control how often the listener needs an update. 0% and 100% will always be dispatched.
//         * @return in percentage (0.2 = call {@link #onProgress} around every 0.2 percent of progress)
//         */
//        float getGranualityPercentage();
//    }
//
//    private static class DispatchingProgressListener implements ResponseProgressListener {
//        private static final Map<String, UIonProgressListener> LISTENERS = new HashMap<>();
//        private static final Map<String, Long> PROGRESSES = new HashMap<>();
//
//        private final Handler handler;
//
//        DispatchingProgressListener() {
//            this.handler = new Handler(Looper.getMainLooper());
//        }
//
//        static void forget(String url) {
//            LISTENERS.remove(url);
//            PROGRESSES.remove(url);
//        }
//
//        static void expect(String url, UIonProgressListener listener) {
//            LISTENERS.put(url, listener);
//        }
//
//        @Override
//        public void update(HttpUrl url, final long bytesRead, final long contentLength) {
//            //System.out.printf("%s: %d/%d = %.2f%%%n", url, bytesRead, contentLength, (100f * bytesRead) / contentLength);
//            String key = url.toString();
//            final UIonProgressListener listener = LISTENERS.get(key);
//            if (listener == null) {
//                return;
//            }
//            if (contentLength <= bytesRead) {
//                forget(key);
//            }
//            if (needsDispatch(key, bytesRead, contentLength, listener.getGranualityPercentage())) {
//                handler.post(() -> listener.onProgress(bytesRead, contentLength));
//            }
//        }
//
//        private boolean needsDispatch(String key, long current, long total, float granularity) {
//            if (granularity == 0 || current == 0 || total == current) {
//                return true;
//            }
//            float percent = 100f * current / total;
//            long currentProgress = (long) (percent / granularity);
//            Long lastProgress = PROGRESSES.get(key);
//            if (lastProgress == null || currentProgress != lastProgress) {
//                PROGRESSES.put(key, currentProgress);
//                return true;
//            } else {
//                return false;
//            }
//        }
//    }
//
//    private static class OkHttpProgressResponseBody extends ResponseBody {
//        private final HttpUrl url;
//        private final ResponseBody responseBody;
//        private final ResponseProgressListener progressListener;
//        private BufferedSource bufferedSource;
//
//        OkHttpProgressResponseBody(HttpUrl url, ResponseBody responseBody,
//                                   ResponseProgressListener progressListener) {
//            this.url = url;
//            this.responseBody = responseBody;
//            this.progressListener = progressListener;
//        }
//
//        @Override
//        public MediaType contentType() {
//            return responseBody.contentType();
//        }
//
//        @Override
//        public long contentLength() {
//            return responseBody.contentLength();
//        }
//
//        @Override
//        public BufferedSource source() {
//            if (bufferedSource == null) {
//                bufferedSource = Okio.buffer(source(responseBody.source()));
//            }
//            return bufferedSource;
//        }
//
//        private Source source(Source source) {
//            return new ForwardingSource(source) {
//                long totalBytesRead = 0L;
//
//                @Override
//                public long read(Buffer sink, long byteCount) throws IOException {
//                    long bytesRead = super.read(sink, byteCount);
//                    long fullLength = responseBody.contentLength();
//                    if (bytesRead == -1) { // this source is exhausted
//                        totalBytesRead = fullLength;
//                    } else {
//                        totalBytesRead += bytesRead;
//                    }
//                    progressListener.update(url, totalBytesRead, fullLength);
//                    return bytesRead;
//                }
//            };
//        }
//    }
}
