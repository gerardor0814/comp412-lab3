public class Main {
    public static void main(String[] args) {
        String helpStatement = """
                Command Syntax:
                        ./schedule filename
                
                Required arguments:
                        filename  is the pathname (absolute or relative) to the input file
                
                Optional flag:
                        -h        prints this message""";
        String fileName = "";
        for (String arg : args) {
            if (arg.equals("-h")) {
                System.out.println(helpStatement);
                return;
            } else {
                if (fileName.isEmpty()) {
                    fileName = arg;
                } else {
                    System.out.println("ERROR:  Attempt to open more than one input file.\n");
                    System.out.println(helpStatement);
                    return;
                }
            }

            Scanner scanner = new Scanner(fileName);
            if (scanner.hasErrors()) {
                return;
            }
            Parser parser = new Parser(scanner);
            run(parser);
        }
    }

    public static void run (Parser parser){
        parser.parse();
        Allocator allocator = new Allocator(parser.getTail(), parser.getHead());
        if (parser.isValid()) {
            allocator.rename(parser.getMaxSR());
            GraphBuilder graphBuilder = new GraphBuilder(parser.getHead());
            graphBuilder.build();
            graphBuilder.addPriorities();
            System.err.println(graphBuilder.makeDotFile());
            graphBuilder.schedule();
        } else {
            System.err.println("Due to syntax errors, run terminates");
        }
    }
}