package ru.lanit;

import io.airlift.airline.Command;
import io.airlift.airline.SingleCommand;


@Command(name = "run", description = "Convert Pdf to Docx")
public class ConverterCLI {

    public static void main(String[] args) {
        SingleCommand<ConverterCLI> parser = SingleCommand.singleCommand(ConverterCLI.class);
        ConverterCLI cmd = parser.parse(args);
        cmd.run();
    }

    private void run() {
        Converter converter = new Converter();
        converter.convert();
    }
}
