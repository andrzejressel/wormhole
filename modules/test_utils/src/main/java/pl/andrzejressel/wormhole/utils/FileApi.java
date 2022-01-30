package pl.andrzejressel.wormhole.utils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.win32.W32APIOptions;

public interface FileApi extends Kernel32 {
    FileApi INSTANCE = Native.load("kernel32", FileApi.class, W32APIOptions.UNICODE_OPTIONS);

    void GetLongPathName(
            String lpszShortPath,
            char[] lpBuffer,
            DWORD cchBuffer
    );

}
