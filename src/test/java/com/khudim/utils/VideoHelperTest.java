package com.khudim.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class VideoHelperTest {

    @Test
    public void shouldCreateTagName() {
        String fileName = VideoHelper.createFileNameWithTags("tmp/file/rere.webm", Arrays.asList("test", "no", "secret"));
        Assert.assertEquals("tmp/file/rere_test:no:secret.webm", fileName);
    }
}
