package com.example.commons;

import java.io.UnsupportedEncodingException;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ChannelKeyUtil {
	
    public  int websiteName2Hash(String name) {
        int num = 0x15051505;
        int num2 = num;

        if (StringUtils.isEmpty(name)) {
            return 0;
        }

        byte[] bytes = null;
        try {
            bytes = name.getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
        if (bytes == null || bytes.length == 0 ){
            return 0;
        }

        int array[] = bytes2int(bytes);

        int j = 0;
        for (int i = name.length(); i > 0; i -= 4) {
            num = (((num << 5) + num) + (num >> 0x1b)) ^ array[j];
            if (i <= 2) {
				break;
			}
            num2 = (((num2 << 5) + num2) + (num2 >> 0x1b)) ^ array[j + 1];
            j += 2;
        }
        return (num + (num2 * 0x5d588b65));
    }
    
 
    private  int[] bytes2int(byte[] bytes) {

        int intLen = bytes.length / 4 + (bytes.length % 4 == 0 ? 0 : 1);
        int[] array = new int[intLen];

        for (int i = 0; i < array.length - 1; i++) {
            array[i] = (bytes[i * 4 + 3] << 24) + ((bytes[i * 4 + 2] & 0xFF) << 16) + ((bytes[i * 4 + 1] & 0xFF) << 8)
                    + (bytes[i * 4] & 0xFF);
        }

        int i = bytes.length % 4;
        switch (i) {
        case 2:
            array[array.length - 1] = ((bytes[bytes.length - 1] & 0xFF) << 8) + (bytes[bytes.length - 2] & 0xFF);
            break;
        default:
            array[array.length - 1] = (bytes[bytes.length - 1] << 24) + ((bytes[bytes.length - 2] & 0xFF) << 16)
                    + ((bytes[bytes.length - 3] & 0xFF) << 8) + (bytes[bytes.length - 4] & 0xFF);
            break;
        }
        return array;
    }
    
    
    public  int weixin2Hash(String name) {
        int num = 0x15051505;
        int num2 = num;

        if (StringUtils.isEmpty(name)) {
            return 0;
        }

        byte[] bytes = null;
        try {
            bytes = name.getBytes("UTF-16LE");
        } catch (UnsupportedEncodingException e) {
        	e.printStackTrace();
        }
        if (bytes == null || bytes.length == 0) {
            return 0;
        }

        int array[] = bytes2int(bytes);

        int j = 0;
        for (int i = name.length(); i > 0; i -= 4) {
            num = (((num << 5) + num) + (num >> 0x1b)) ^ array[j];
            if (i <= 2) {
				break;
			}
            num2 = (((num2 << 5) + num2) + (num2 >> 0x1b)) ^ array[j + 1];
            j += 2;
        }
        return (num + (num2 * 0x5d588b65));
    }
}
