package com.buildhappy.test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Created by buildhappy on 15/9/24.
 */
public class TestNashorn {
    public static void main(String[] args){
        long start = System.currentTimeMillis();
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");//javascript nashorn
        for(int i = 0; i < 10; i++){

            try{
                //jdk8 Nashon
                //官网文档http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html
                String javaScript = "function f() { return 1; }; f() + 1;";
                //System.out.println( "Result:" + engine.eval(javaScript) );
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println("Use time:" + time);//76-680
    }
}
