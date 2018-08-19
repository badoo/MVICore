package com.badoo.catapi;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(strict = false)
public class Response {

    @Element
    @Path("data/images/image")
    public String url;
}
