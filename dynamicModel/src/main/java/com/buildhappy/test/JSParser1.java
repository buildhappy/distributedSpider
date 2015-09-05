package com.buildhappy.test;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.CompiledScript;

import org.mozilla.javascript.Context;

public class JSParser1 {
	@SuppressWarnings("restriction")
	public static void main(String[] args){
		String javaScript =  "function f() { return 1; }; f() + 1;";
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		
		Map<String , CompiledScript> scripts = new HashMap<String , CompiledScript>();
		CompiledScript testScript = scripts.get("test");
		if(testScript == null){
			//Compilable compileEngine = (Compilable)engine;
		}
		try{
			//jdk8 Nashon
			//官网文档http://www.oracle.com/technetwork/articles/java/jf14-nashorn-2126515.html
			//ScriptEngineManager manager = new ScriptEngineManager();
			//ScriptEngine engine = manager.getEngineByName("JavaScript");
			//System.out.println( "Result:" + engine.eval( javaScript) );
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			
		}
	}
	
	
	public void test(){
		Map<String, CompiledScript> m = new HashMap<String, CompiledScript>();
		// ...
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");
		CompiledScript script = m.get("fib");
		if(script == null) {
			Context cx = new Context();
			//cx.compileString(script, script, 12, null);
			//Compilable compilingEngine = (Compilable)engine;
//			script = compilingEngine.compile(
//					"fib(num);" +
//					"function fib(n) {" +
//					"  if(n <= 1) return n; " +
//					"  return fib(n-1) + fib(n-2); " +
//					"};"		
//			);
			m.put("fib", script);
		}
		Bindings bindings = engine.createBindings();
		bindings.put("num", "20");
//		Object result = script.eval(bindings);
//		System.out.println(result);
	}
}
