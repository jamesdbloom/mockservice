package org.mockserver.streams;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import javax.servlet.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockserver.character.Character.NEW_LINE;

/**
 * @author jamesdbloom
 */
public class IOStreamUtilsTest {


    @Test
    public void shouldReadSocketInputStreamToString() throws IOException {
        // given
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("bytes", UTF_8));

        // when
        String result = new IOStreamUtils().readInputStreamToString(socket);

        // then
        assertEquals("bytes\n", result);
    }

    @Test
    public void shouldReadHttpRequestOnSocketInputStreamToString() throws IOException {
        // given
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                        "Cache-Control:public, max-age=60\r" + NEW_LINE +
                        "Content-Length:10\r" + NEW_LINE +
                        "Content-Type:text/html; charset=utf-8\r" + NEW_LINE +
                        "Date:Sat, 04 Jan 2014 17:18:54 GMT\r" + NEW_LINE +
                        "Expires:Sat, 04 Jan 2014 17:19:54 GMT\r" + NEW_LINE +
                        "Last-Modified:Sat, 04 Jan 2014 17:18:54 GMT\r" + NEW_LINE +
                        "Vary:*" + NEW_LINE +
                        "\r" + NEW_LINE +
                        "1234567890",
                UTF_8));

        // when
        String result = IOStreamUtils.readInputStreamToString(socket);

        // then
        assertEquals("" +
                "Cache-Control:public, max-age=60" + NEW_LINE +
                "Content-Length:10" + NEW_LINE +
                "Content-Type:text/html; charset=utf-8" + NEW_LINE +
                "Date:Sat, 04 Jan 2014 17:18:54 GMT" + NEW_LINE +
                "Expires:Sat, 04 Jan 2014 17:19:54 GMT" + NEW_LINE +
                "Last-Modified:Sat, 04 Jan 2014 17:18:54 GMT" + NEW_LINE +
                "Vary:*" + NEW_LINE +
                "" + NEW_LINE +
                "1234567890", result);
    }

    @Test
    public void shouldReadHttpRequestOnSocketInputStreamToStringLowerCaseHeaders() throws IOException {
        // given
        Socket socket = mock(Socket.class);
        when(socket.getInputStream()).thenReturn(IOUtils.toInputStream("" +
                        "cache-control:public, max-age=60\r" + NEW_LINE +
                        "content-length:10\r" + NEW_LINE +
                        "content-type:text/html; charset=utf-8\r" + NEW_LINE +
                        "date:Sat, 04 Jan 2014 17:18:54 GMT\r" + NEW_LINE +
                        "expires:Sat, 04 Jan 2014 17:19:54 GMT\r" + NEW_LINE +
                        "last-modified:Sat, 04 Jan 2014 17:18:54 GMT\r" + NEW_LINE +
                        "vary:*" + NEW_LINE +
                        "\r" + NEW_LINE +
                        "1234567890",
                UTF_8));

        // when
        String result = IOStreamUtils.readInputStreamToString(socket);

        // then
        assertEquals("" +
                "cache-control:public, max-age=60" + NEW_LINE +
                "content-length:10" + NEW_LINE +
                "content-type:text/html; charset=utf-8" + NEW_LINE +
                "date:Sat, 04 Jan 2014 17:18:54 GMT" + NEW_LINE +
                "expires:Sat, 04 Jan 2014 17:19:54 GMT" + NEW_LINE +
                "last-modified:Sat, 04 Jan 2014 17:18:54 GMT" + NEW_LINE +
                "vary:*" + NEW_LINE +
                "" + NEW_LINE +
                "1234567890", result);
    }

    @Test
    public void shouldReadServletRequestInputStreamToString() throws IOException {
        // given
        ServletRequest servletRequest = mock(ServletRequest.class);
        when(servletRequest.getInputStream()).thenReturn(
                new DelegatingServletInputStream(IOUtils.toInputStream("bytes", UTF_8))
        );

        // when
        String result = IOStreamUtils.readInputStreamToString(servletRequest);

        // then
        assertEquals("bytes", result);
    }

    @Test(expected = RuntimeException.class)
    public void shouldHandleExceptionWhenReadingServletRequestInputStreamToString() throws IOException {
        // given
        ServletRequest servletRequest = mock(ServletRequest.class);
        when(servletRequest.getInputStream()).thenThrow(new IOException("TEST EXCEPTION"));

        // when
        IOStreamUtils.readInputStreamToString(servletRequest);
    }

    @Test
    public void shouldReadInputStreamToByteArray() throws IOException {
        // given
        ServletRequest servletRequest = mock(ServletRequest.class);
        when(servletRequest.getInputStream()).thenReturn(
                new DelegatingServletInputStream(IOUtils.toInputStream("bytes", UTF_8))
        );

        // when
        byte[] result = IOStreamUtils.readInputStreamToByteArray(servletRequest);

        // then
        assertEquals("bytes", new String(result));
    }

    @Test(expected = RuntimeException.class)
    public void shouldHandleExceptionWhenReadInputStreamToByteArray() throws IOException {
        // given
        ServletRequest servletRequest = mock(ServletRequest.class);
        when(servletRequest.getInputStream()).thenThrow(new IOException("TEST EXCEPTION"));

        // when
        byte[] result = IOStreamUtils.readInputStreamToByteArray(servletRequest);

        // then
        assertEquals("bytes", new String(result));
    }

    @Test
    public void shouldWriteToOutputStream() throws IOException {
        // given
        ServletResponse mockServletResponse = mock(ServletResponse.class);
        ServletOutputStream mockServletOutputStream = mock(ServletOutputStream.class);
        when(mockServletResponse.getOutputStream()).thenReturn(mockServletOutputStream);

        // when
        IOStreamUtils.writeToOutputStream("data".getBytes(UTF_8), mockServletResponse);

        // then
        verify(mockServletOutputStream).write("data".getBytes(UTF_8));
        verify(mockServletOutputStream).close();
    }

    @Test(expected = RuntimeException.class)
    public void shouldHandleExceptionWriteToOutputStream() throws IOException {
        // given
        ServletResponse mockServletResponse = mock(ServletResponse.class);
        when(mockServletResponse.getOutputStream()).thenThrow(new IOException("TEST EXCEPTION"));

        // when
        IOStreamUtils.writeToOutputStream("data".getBytes(UTF_8), mockServletResponse);
    }

    @Test
    public void shouldCreateBasicByteBuffer() {
        // when
        ByteBuffer byteBuffer = IOStreamUtils.createBasicByteBuffer("byte_buffer");

        // then
        byte[] content = new byte[byteBuffer.limit()];
        byteBuffer.get(content);
        assertEquals("byte_buffer", new String(content));
    }

    class DelegatingServletInputStream extends ServletInputStream {

        private final InputStream inputStream;

        DelegatingServletInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public int read() throws IOException {
            return this.inputStream.read();
        }

        public void close() throws IOException {
            super.close();
            this.inputStream.close();
        }

        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }
    }
}
