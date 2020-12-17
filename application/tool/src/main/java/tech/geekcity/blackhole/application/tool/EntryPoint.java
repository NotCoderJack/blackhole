package tech.geekcity.blackhole.application.tool;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Arrays;

public class EntryPoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class.getName());

    public static void main(String... args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Preconditions.checkArgument(
                args.length > 0,
                "class name should be specified with the first argument");
        String className = args[0];
        Class<?> clazz = Class.forName(className);
        LOGGER.info("class({}) found", clazz.getName());
        String[] remainedArguments = Arrays.stream(args)
                .skip(1)
                .toArray(String[]::new);
        LOGGER.info(
                "passing arguments({}) to class({})",
                String.join(" ", remainedArguments),
                clazz.getName());
        int exitCode = new CommandLine(clazz.newInstance())
                .execute(remainedArguments);
        System.exit(exitCode);
    }
}
