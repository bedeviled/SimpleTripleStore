package com.jthalbert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class HelloWorld
{
    public static void main( String[] args )
    {
        Logger logger = LoggerFactory.getLogger(HelloWorld.class);
        logger.info("Hello World");
        System.out.println( "Hello World!" );
    }
}
