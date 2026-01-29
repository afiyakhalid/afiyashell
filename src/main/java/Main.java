import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;  
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;



//added history index
public class Main {
    private static Path current=Paths.get(System.getProperty("user.dir"));
     private static List<String> history=new ArrayList<>();
     private static int historyWriteIndex = 0;
    public static void main(String[] args) throws Exception {
   
      String historyfile = System.getenv("HISTFILE");
if (historyfile != null && !historyfile.isBlank()) {
    Path histPath = Paths.get(historyfile);
    if (!histPath.isAbsolute()) histPath = current.resolve(histPath).normalize();
    if (Files.exists(histPath) && Files.isReadable(histPath)) {
        try (Stream<String> lines = Files.lines(histPath, java.nio.charset.StandardCharsets.UTF_8)) {
            lines.forEach(l -> {
                if (l == null) return;
                String t = l.replace("\r", "").trim();
                if (t.isEmpty()) return;                     
                t = t.replaceFirst("^\\s*\\d+\\s+", "");     
                if (!t.isEmpty()) history.add(t);
            });
          
            historyWriteIndex = history.size();
        } catch (IOException e) {
            System.err.println("warning: unable to read HISTFILE " + histPath + ": " + e.getMessage());
        }
    }
}
      
        List<String> builtins=Arrays.asList("echo","exit","type","pwd","cd","history");
     boolean interactive = hasTty();
        

        
        if (!interactive) {
    Scanner scanner = new Scanner(System.in);
    while (true) {
        System.out.print("$ ");
        System.out.flush();

        if (!scanner.hasNextLine()) break; // EOF

        String input = scanner.nextLine().trim();
        if (input.equals("exit")) break;
       if (!input.isEmpty()) {
    history.add(input);      
}

        handleCommand(input, builtins, System.in, System.out);
    
    }
    return;
        }

        
        while(true){
          
               System.out.print("$ ");
            System.out.flush();
             setRawMode(true);
             StringBuilder inputbuffer=new StringBuilder();
             int historyIndex=history.size();
            
            
               String lastTabPrefix=null;
                boolean tabPending=false;
             while(true){
                int c=System.in.read();
                 if(c==27){
                int next1=System.in.read();
                if(next1==91){
                    int next2=System.in.read();
                    if(next2==65){
                        if(historyIndex>0){
                            historyIndex--;
                            String prevCmd = history.get(historyIndex);
                            // System.out.print("033[2K\r$ " + prevCmd);
                            System.out.print("\r\u001b[2K$ " + prevCmd);
                            System.out.flush();
                            inputbuffer.setLength(0);
                            inputbuffer.append(prevCmd);
                        }
                        }//down arrow
                        else if (next2 == 66) { 
                            if (historyIndex < history.size()) {
                                historyIndex++;
                                String nextCmd = (historyIndex == history.size()) ? "" : history.get(historyIndex);
                                
                                // System.out.print("\033[2K\r$ " + nextCmd);
                                  System.out.print("\r\u001b[2K$ " + nextCmd);
                                System.out.flush();
                                
                                inputbuffer.setLength(0);
                                inputbuffer.append(nextCmd);
                            }
                        }
                        
                        tabPending = false;
                        lastTabPrefix = null;
                        continue;
                    }
                }//lets hope this works
                if(c==9){
                    String line = inputbuffer.toString();
                    int spaceIdx = line.indexOf(' ');
    if (spaceIdx != -1) {
     
        continue;
    }
                    Set<String> allcommands=getAllCommands(builtins);
                    String currentinput=inputbuffer.toString();
                    List<String> candidates=new ArrayList<>();
                    for (String cmd : allcommands) {
         if (cmd.startsWith(currentinput)) {
            candidates.add(cmd);
        }
    }
    Collections.sort(candidates);
    
                  if(candidates.isEmpty()){
                    System.out.print("\u0007");
                    System.out.flush();
                    tabPending=false;
                    lastTabPrefix=null;
}                   else if(candidates.size()==1){
                        String matches=candidates.get(0);
                       String suffix = matches.substring(inputbuffer.length()) + " ";
                        System.out.print(suffix);
                        inputbuffer.append(suffix);
                    tabPending = false;
                    lastTabPrefix = null;

                    }
                    else {
       
        String commonPrefix = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            String next = candidates.get(i);
            while (!next.startsWith(commonPrefix)) {
                commonPrefix = commonPrefix.substring(0, commonPrefix.length() - 1);
            }
        }

      
        if (commonPrefix.length() > currentinput.length()) {
            String suffix = commonPrefix.substring(currentinput.length());
            System.out.print(suffix);
            inputbuffer.append(suffix);
         
            tabPending = false; 
            lastTabPrefix = null;
        }
                    
                else{
                    if(tabPending && line.equals(lastTabPrefix)){
                       System.out.print("\r\n");
                        System.out.print(String.join("  ", candidates));
                        System.out.print("\r\n");
                        System.out.print("$ ");
                        System.out.print(line);
                        System.out.flush();
                        tabPending=false;
                        lastTabPrefix=null;
                    }else{
                        
                        System.out.print("\u0007");
                        System.out.flush();
                        tabPending=true;
                        lastTabPrefix=line;

                    }
                    }
                    }
                    continue;
                    }else if (c==127) {
                   if(inputbuffer.length()>0){
                    inputbuffer.deleteCharAt(inputbuffer.length()-1);
                    // System.out.print("\b \b");
                   }
                    tabPending = false;
                      lastTabPrefix = null;
                }else if(c==10||c==13){
                    System.out.print("\r\n");
                      System.out.flush();
                    break;

                }
                else{
                    char ch=(char)c;
                      System.out.print(ch);
                 System.out.flush();
                    inputbuffer.append(ch);
                     tabPending = false;
                      lastTabPrefix = null;
                }
             }
             setRawMode(false);
                String input = inputbuffer.toString().trim();
                if (!input.isEmpty()) {
            history.add(input);
        }
            if (input.equals("exit")) {

//             String histfile = System.getenv("HISTFILE");
//     if (histfile != null && !histfile.isBlank()) {
//         Path path = Paths.get(histfile);
//         if (!path.isAbsolute()) path = current.resolve(path).normalize();
//         try {
//             if (path.getParent() != null) Files.createDirectories(path.getParent());
//             Files.write(path, history, java.nio.charset.StandardCharsets.UTF_8,
//                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
//             historyWriteIndex = history.size();
//         } catch (IOException e) {
//             System.err.println("warning: unable to write HISTFILE " + path + ": " + e.getMessage());
//         }
//     }
//     System.exit(0);
// }
String histfile = System.getenv("HISTFILE");
    if (histfile != null && !histfile.isBlank()) {
        Path path = Paths.get(histfile);
        if (!path.isAbsolute()) path = current.resolve(path).normalize();
        
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());

            // 
            if (historyWriteIndex < history.size()) {
                List<String> toAppend = history.subList(historyWriteIndex, history.size());
                
                Files.write(path, toAppend, java.nio.charset.StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, 
                    StandardOpenOption.APPEND, // <--- Use APPEND, not TRUNCATE
                    StandardOpenOption.WRITE);
            }
        } catch (IOException e) {
            System.err.println("warning: unable to write HISTFILE " + path + ": " + e.getMessage());
        }
    }
    System.exit(0);
}



            handleCommand(input, builtins, System.in, System.out);
        }
    }
      private static boolean hasTty() {
        // try {
        //     Path tty = Path.of("/dev/tty");
        //     return Files.isReadable(tty) && Files.isWritable(tty);
        // } catch (Exception e) {
        //     return false;
        // }
    //     try {
    //     if (System.console() == null) return false;

    //     String in = Path.of("/dev/stdin").toRealPath().toString();
    //     String out = Path.of("/dev/stdout").toRealPath().toString();

    //     boolean inIsTty = in.startsWith("/dev/pts/") || in.startsWith("/dev/tty");
    //     boolean outIsTty = out.startsWith("/dev/pts/") || out.startsWith("/dev/tty");

    //     return inIsTty && outIsTty;
    // } catch (Exception e) {

    // }
    //     return false;
    //   }

    // try {
    //     if (System.console() != null) return true;
    // } catch (Throwable ignored) {}

    // // 2) Unix fallback: check /dev/stdin and /dev/stdout
    // try {
    //     Path in = Path.of("/dev/stdin").toRealPath();
    //     Path out = Path.of("/dev/stdout").toRealPath();
    //     boolean inIsTty = in.toString().startsWith("/dev/pts/") || in.toString().startsWith("/dev/tty");
    //     boolean outIsTty = out.toString().startsWith("/dev/pts/") || out.toString().startsWith("/dev/tty");
    //     if (inIsTty && outIsTty) return true;
    // } catch (Throwable ignored) {
    //     // ignore; fall through to last resort
    // }

    // // 3) Last resort: /dev/tty accessibility
    // try {
    //     Path devTty = Path.of("/dev/tty");
    //     if (Files.exists(devTty) && Files.isReadable(devTty) && Files.isWritable(devTty)) return true;
    // } catch (Throwable ignored) {}

    // return false;

    //  try {
    //     if (System.console() != null) return true;
    // } catch (Throwable ignored) {}

    // try {
    //     Path fd0 = Path.of("/proc/self/fd/0");
    //     if (Files.exists(fd0) && Files.isSymbolicLink(fd0)) {
    //         String target = Files.readSymbolicLink(fd0).toString();
    //         if (target.contains("/dev/pts") || target.contains("/dev/tty")) return true;
    //     }
    // } catch (Throwable ignored) {}

    // try {
    //     Path stdin = Path.of("/dev/stdin");
    //     Path stdout = Path.of("/dev/stdout");
    //     if (Files.exists(stdin) && Files.exists(stdout)) {
    //         String in = stdin.toRealPath().toString();
    //         String out = stdout.toRealPath().toString();
    //         if ((in.startsWith("/dev/pts/") || in.startsWith("/dev/tty")) &&
    //             (out.startsWith("/dev/pts/") || out.startsWith("/dev/tty"))) {
    //             return true;
    //         }
    //     }
    // } catch (Throwable ignored) {}

    // try {
    //     Path devTty = Path.of("/dev/tty");
    //     if (Files.exists(devTty) && Files.isReadable(devTty) && Files.isWritable(devTty)) return true;
    // } catch (Throwable ignored) {}

    // return false;}

   

    try {
        if (System.console() != null) return true;
    } catch (Throwable ignored) {}

    // 2) /proc/self/fd/0 -> real target (works on most Linux containers)
    try {
        Path fd0 = Path.of("/proc/self/fd/0");
        if (Files.exists(fd0) && Files.isSymbolicLink(fd0)) {
            Path target = Files.readSymbolicLink(fd0);
            String ts = target.toString();
            if (ts.contains("/dev/pts/") || ts.contains("/dev/tty")) return true;
        }
    } catch (Throwable ignored) {}

    // 3) /dev/stdin and /dev/stdout realpath check
    try {
        Path stdin = Path.of("/dev/stdin");
        Path stdout = Path.of("/dev/stdout");
        if (Files.exists(stdin) && Files.exists(stdout)) {
            String in = stdin.toRealPath().toString();
            String out = stdout.toRealPath().toString();
            if ((in.startsWith("/dev/pts/") || in.startsWith("/dev/tty")) &&
                (out.startsWith("/dev/pts/") || out.startsWith("/dev/tty"))) {
                return true;
            }
        }
    } catch (Throwable ignored) {}

    // 4) /dev/tty accessibility
    try {
        Path devTty = Path.of("/dev/tty");
        if (Files.exists(devTty) && Files.isReadable(devTty) && Files.isWritable(devTty)) return true;
    } catch (Throwable ignored) {}

    // 5) Shell test fallback: test -t 0 && test -t 1  (run with a short timeout)
    try {
        Process p = new ProcessBuilder("sh", "-c", "test -t 0 && test -t 1").redirectErrorStream(true).start();
        boolean finished = p.waitFor(150, java.util.concurrent.TimeUnit.MILLISECONDS);
        if (finished && p.exitValue() == 0) return true;
        // if it didn't finish quickly, destroy it to avoid hanging
        if (p.isAlive()) {
            p.destroy();
        }
    } catch (Throwable ignored) {}

    return false;
} 
handling commands
if (System.console() != null) {
        return true;
    }

  
    try {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", "test -t 0");
        Process p = pb.start();
        int exitCode = p.waitFor();
        return exitCode == 0;
    } catch (Exception e) {
        
        return false;
    }
}



private static void handleCommand(String input, List<String> builtins, java.io.InputStream stdin, java.io.OutputStream stdout) throws Exception {
    java.io.PrintStream out = new java.io.PrintStream(stdout, true, "UTF-8");
        if (input.isEmpty()) return;

// if (input.contains("|")) {
//             String[] parts = input.split("\\|", 2);
//             String left = parts[0].trim();
//             String right = parts[1].trim();

//             // If both sides are external commands, run as real processes and stream between them.
//             String[] leftArgs = parseArguments(left);
//             String[] rightArgs = parseArguments(right);

//             boolean leftExternal = leftArgs.length > 0 && getpath(leftArgs[0]) != null;
//             boolean rightExternal = rightArgs.length > 0 && getpath(rightArgs[0]) != null;

//             if (leftExternal && rightExternal) {
//                 runExternalPipe(leftArgs, rightArgs, stdin, stdout);
//                 return;
//             }

//             // Fallback to your old recursive piping for non-external cases (finite output)
//             java.io.PipedOutputStream pipeOut = new java.io.PipedOutputStream();
//             java.io.PipedInputStream pipeIn = new java.io.PipedInputStream(pipeOut);

//             Thread sourceThread = new Thread(() -> {
//                  try {
//                     handleCommand(left, builtins, stdin, pipeOut);
//                 } catch (Exception ignored) {
//                 } finally {
//                     try { pipeOut.close(); } catch (Exception ignored) {}
//                 }
//             });
//             sourceThread.start();

//             handleCommand(right, builtins, pipeIn, stdout);

//             // IMPORTANT: don't deadlock forever waiting for a command like `tail -f`
//             sourceThread.join(200);
//             return;
//         }
// [Inside handleCommand]
// [Inside handleCommand]
if (input.contains("|")) {
    String[] parts = input.split("\\|");
        List<String> rawCommands = new ArrayList<>();
        for (String part : parts) rawCommands.add(part.trim());

        if (rawCommands.size() == 2) {
            String leftCmd = rawCommands.get(0);
            String rightCmd = rawCommands.get(1);

            String[] leftArgs = parseArguments(leftCmd);
            String[] rightArgs = parseArguments(rightCmd);

            boolean leftExternal = leftArgs.length > 0 && getpath(leftArgs[0]) != null;
            boolean rightExternal = rightArgs.length > 0 && getpath(rightArgs[0]) != null;

            if (leftExternal && rightExternal) {
                runExternalPipe(leftArgs, rightArgs, stdin, stdout);
                return;
            }
        }

        
        runMultiPipeline(rawCommands, builtins, stdin, stdout);
        return;
}
    //hello   
     if (input.startsWith("echo "))
        {
        String[] parts=parseArguments(input);
           java.io.File outputfile=null;
           java.io.File errorFile = null;
           boolean append=false;
           List<String> argstoadd=new ArrayList<>();
           for(int i=1;i<parts.length;i++){
            if(parts[i].equals(">")||parts[i].equals("1>")){
                if(i+1<parts.length)
{
    outputfile=new java.io.File(parts[i+1]);
    append=false;
    i++;
}            }

else if(parts[i].equals(">>")||parts[i].equals("1>>")){
                if(i+1<parts.length)
{
    outputfile=new java.io.File(parts[i+1]);
    append=true;
    i++;

}
}else if (parts[i].equals("2>")) {  
            if (i + 1 < parts.length) {
                errorFile = new java.io.File(parts[i+1]);
                i++;
            }
        }
        else if (parts[i].equals("2>>")) {  
            if (i + 1 < parts.length) {
                errorFile = new java.io.File(parts[i+1]);
                i++;
            }
        }
        
else{
    argstoadd.add(parts[i]);
}
           }
           if(outputfile!=null){
            try(java.io.PrintWriter ps=new java.io.PrintWriter(new java.io.FileOutputStream(outputfile,append))){
                for(int j=0;j<argstoadd.size();j++){
                    ps.print(argstoadd.get(j));
                    if(j<argstoadd.size()-1){
                        ps.print(" ");
                    }
                }
                ps.println();
            }
        }
        else{
            for(int j=0;j<argstoadd.size();j++){
                out.print(argstoadd.get(j));
                if(j<argstoadd.size()-1){
                    out.print(" ");
                }
            }
            out.println();
        }
        if (errorFile != null) {
                    try {
                       
                        new java.io.FileOutputStream(errorFile).close();
                    } catch (java.io.IOException e) {
                        
                    }
                }

            
        }
        
                else if (input.startsWith("type ")) {
                String commandtocheck = input.substring(5);
        if(builtins.contains(commandtocheck))
        {
            out.println(commandtocheck +" is a shell builtin");
           

        }else{
          String path=getpath(commandtocheck);
          if(path!=null){
            out.println(commandtocheck + " is " + path);
          }else{
        out.println(commandtocheck + ": not found");
        }
        } 
       }else if (input.startsWith("history")) {

        //last history command
        int n=history.size();
        String [] args=parseArguments(input);
       
//         if (args.length >= 2 && args[1].equals("-r")) {
//     if (args.length < 3) {
//         out.println("history: -r requires a file operand");
//         return;
//     }
//     String filepath = args[2];
//     Path path = Paths.get(filepath);
//     if (!path.isAbsolute()) {
//         path = current.resolve(path).normalize();
//     }
//     if (Files.exists(path) && Files.isReadable(path)) {
//         try {
//             List<String> lines = Files.readAllLines(path);
//             for (String l : lines) {
//                 if (l == null) continue;
                
//                 String t = l.replace("\r", "").trim();
//                 if (t.isEmpty()) continue;
              
//                 t = t.replaceFirst("^\\s*\\d+\\s+", "");
//                 history.add(t); 
//             }
//         } catch (IOException e) {
//             out.println("history: " + filepath + ": Unable to read file");
//         }
//     } else {
//         out.println("history: " + filepath + ": No such file or directory");
//     }
//     return;
//         }
//          else if (args.length >= 2 && args[1].equals("-w")) {
//     if (args.length < 3) {
//         out.println("history: -w requires a file operand");
//         return;
//     }
    
//     String filepath = args[2];
//     Path path = Paths.get(filepath);
//     if (!path.isAbsolute()) {
//         path = current.resolve(path).normalize();
//     }
//     try {
//         Path parent = path.getParent();
//         if (parent != null) {
//             Files.createDirectories(parent);
//         }
        
//         try (java.io.BufferedWriter bw = Files.newBufferedWriter(path, java.nio.charset.StandardCharsets.UTF_8)) {
//             for (String h : history) {
//                 if (h == null) continue;
//                 bw.write(h);
//                  if (h.trim().isEmpty()) continue;
//                 bw.newLine();
//             }
            
           
//         }
//     } catch (IOException e) {
//         out.println("history: " + filepath + ": Unable to write file");
//     }
//     return; 
// }
 
//  else if(args.length>=2&&args[1].equals("-a")){
// if (args.length < 3) {
//         out.println("history: -a requires a file operand");
//         return;
//     }
//     String filepath = args[2];
//     Path path = Paths.get(filepath);
//     if (!path.isAbsolute()) {
//         path = current.resolve(path).normalize();
//     }
//     try {
//       if (path.getParent() != null) Files.createDirectories(path.getParent());
            
            
//             int existing = 0;
//         if (Files.exists(path) && Files.isReadable(path)) {
//             try (Stream<String> lines = Files.lines(path, java.nio.charset.StandardCharsets.UTF_8)) {
//                 existing = (int) lines.filter(s -> !s.trim().isEmpty()).count();
//             }
//         }

//         // Append only the new entries
//         int from = Math.max(0, Math.min(existing, history.size()));
//         if (from < history.size()) {
//             List<String> toAppend = new ArrayList<>(history.subList(from, history.size()));
//             Files.write(path, toAppend, java.nio.charset.StandardCharsets.UTF_8,
//                 java.nio.file.StandardOpenOption.CREATE,
//                 java.nio.file.StandardOpenOption.APPEND,
//                 java.nio.file.StandardOpenOption.WRITE);
//         }
//     }  catch (IOException e) {
//         out.println("history: " + filepath + ": Unable to write file");
//     }
//     return;
// }
// HISTORY READ
            if (args.length >= 2 && args[1].equals("-r")) {
                if (args.length < 3) {
                    out.println("history: -r requires a file operand");
                    return;
                }
                Path path = Paths.get(args[2]);
                if (!path.isAbsolute()) path = current.resolve(path).normalize();
                
                if (Files.exists(path) && Files.isReadable(path)) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        for (String l : lines) {
                            if (l == null) continue;
                            String t = l.trim().replaceAll("^\\s*\\d+\\s+", "");
                            if (!t.isEmpty()) history.add(t);
                        }
                      historyWriteIndex = history.size();
                        // If we read history from a file, we can optionally advance the writeIndex 
                        // so we don't duplicate these back to disk, or leave it to duplicate. 
                        // Standard bash usually appends file content to memory.
                    } catch (IOException e) {
                        out.println("history: " + args[2] + ": Unable to read file");
                    }
                } else {
                    out.println("history: " + args[2] + ": No such file or directory");
                }
                 
            }
            // HISTORY WRITE (Truncate)
            else if (args.length >= 2 && args[1].equals("-w")) {
                if (args.length < 3) {
                    out.println("history: -w requires a file operand");
                    return;
                }
                Path path = Paths.get(args[2]);
                if (!path.isAbsolute()) path = current.resolve(path).normalize();
                try {
                    if (path.getParent() != null) Files.createDirectories(path.getParent());
                    Files.write(path, history, java.nio.charset.StandardCharsets.UTF_8,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
                              historyWriteIndex = history.size();
                } 
                catch (IOException e) {
                    out.println("history: " + args[2] + ": Unable to write file");
                }
                
            } 
            // HISTORY APPEND (The fix is here)
            else if (args.length >= 2 && args[1].equals("-a")) {
                if (args.length < 3) {
                    out.println("history: -a requires a file operand");
                    return;
                }
                Path path = Paths.get(args[2]);
                if (!path.isAbsolute()) path = current.resolve(path).normalize();

                try {
                    if (path.getParent() != null) Files.createDirectories(path.getParent());

                    // FIX: Append only what hasn't been written in this session
                    // We DO NOT check the file's existing lines to calculate offset
                    if (historyWriteIndex < history.size()) {
                        List<String> toAppend = history.subList(historyWriteIndex, history.size());
                        Files.write(path, toAppend, java.nio.charset.StandardCharsets.UTF_8,
                                StandardOpenOption.CREATE, StandardOpenOption.APPEND, StandardOpenOption.WRITE);
                        
                        // Update the index so next time we don't write duplicates
                        historyWriteIndex = history.size();
                    }
                } catch (IOException e) {
                    out.println("history: " + args[2] + ": Unable to write file");
                }
            }else{
        if (args.length == 1) {
        for (int i = 0; i < history.size(); i++) {
            out.println("    " + (i + 1) + "  " + history.get(i));
        }
        return;
    }
        if(args.length>1){
            try{
            n=Integer.parseInt(args[1]);
            }catch(NumberFormatException e){
               
            }
            
        
           int startindex=Math.max(0,history.size()-n);
    for (int i = startindex; i < history.size(); i++) {
        
        out.println("    " + (i + 1) + "  " + history.get(i));
    }
        }
}
        
    
}
       //what iswromg here
       else if(input.equals("pwd")) {
      
        out.println(current.toString());
        }else if(input.startsWith("cd")) {
            String pathstring =input.substring(3);
            if(pathstring.equals("~")){
                pathstring=System.getenv("HOME");
             }else if(pathstring.startsWith("~/")){
                pathstring=System.getenv("HOME") + pathstring.substring(1);
             }
            Path newpath=current.resolve(pathstring).normalize();
            if(Files.isDirectory(newpath)){
                current=newpath;
            }else{
                out.println("cd: " + pathstring + ": No such file or directory");
            }
        }else{
              
                String[] parts = parseArguments(input);
                List<String> commandargs = new ArrayList<>();
                java.io.File outputfile = null;
                java.io.File errorfile = null;
                boolean append=false;

                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].equals(">") || parts[i].equals("1>")) {
                        if (i + 1 < parts.length) {
                            outputfile = new java.io.File(parts[i + 1]);
                            append=false;
                            i++;
                        }
                    } else if (parts[i].equals("2>")) {
                        if (i + 1 < parts.length) {
                            errorfile = new java.io.File(parts[i + 1]);
                            i++;
                        }
                    }
                    else if (parts[i].equals("2>>")) {
                        if (i + 1 < parts.length) {
                            errorfile = new java.io.File(parts[i + 1]);
                            append=true;
                            i++;
                        }
                    }else if (parts[i].equals(">>") || parts[i].equals("1>>")) {
                        if (i + 1 < parts.length) {
                            outputfile = new java.io.File(parts[i + 1]);
                            append = true;
                            i++;
                        }
                        
                    }
                     else {
                        commandargs.add(parts[i]);
                    }
                }

                if (commandargs.size() > 0) {
                    String command = commandargs.get(0);
                    String commandpath = getpath(command);
    
    if (commandpath != null) {
                    ProcessBuilder pb = new ProcessBuilder(commandargs);
                    pb.directory(current.toFile());
                if (stdin == System.in) {
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
    } else {
        pb.redirectInput(ProcessBuilder.Redirect.PIPE);
    }
                    boolean manualOutput = false;

                    if (outputfile != null) {
                        if (append) {
                            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(outputfile));
                        } else {
                            pb.redirectOutput(outputfile);
                        }
                    } else {
          
                        manualOutput = true;
                    }

                  
                    if (errorfile != null) {
                        if (append) {
                            pb.redirectError(ProcessBuilder.Redirect.appendTo(errorfile));
                        } else {
                            pb.redirectError(errorfile);
                        }
                    } else {
                        
                        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                    }

                    Process process = pb.start();

                  
               

if (stdin != System.in) {
    new Thread(() -> {
        try (java.io.OutputStream procIn = process.getOutputStream()) {
            stdin.transferTo(procIn);
            procIn.close(); 
        } catch (java.io.IOException e) {
            
        }
    }).start();
}

if (manualOutput) {
    try (InputStream pis = process.getInputStream()) {
        byte[] buffer = new byte[1024];
        int bytesRead;
     
        while ((bytesRead = pis.read(buffer)) != -1) {
            stdout.write(buffer, 0, bytesRead);
            stdout.flush(); // FORCE DATA TO NEXT COMMAND
        }
    } catch (IOException e) {
        
        process.destroyForcibly();
    }
}


process.waitFor();}
else {
    out.println(command + ": not found");
}
                }
            }
        }
    
    
    private static void setRawMode(boolean enable) {
        
        String[] cmd = enable 
            ? new String[]{"/bin/sh", "-c", "stty -echo raw </dev/tty"}
            : new String[]{"/bin/sh", "-c", "stty echo -raw </dev/tty"};
        
        try {
            Runtime.getRuntime().exec(cmd).waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

private static String[] parseArguments(String input){
    List<String> args=new ArrayList<>();
    StringBuilder current=new StringBuilder();
    boolean issingle=false;
    boolean isdouble=false;
    for(int i=0;i<input.length();i++){
        char c=input.charAt(i);
        if(c=='\\'){
           if(issingle){
            current.append(c);
           }else if(isdouble){
            if(i+1<input.length()){
                char next=input.charAt(i+1);
                if(next=='"'||next=='\\'||next=='$'||next=='`'){
                    current.append(next);
                    i++;
                }else{
                    current.append(c);
                }
            }else{
                current.append(c);

                }
            }
            else{
                if(i+1<input.length()){
                    current.append(input.charAt(i+1));
                i++;
            }
           }
           continue;
        }
        
        if(c=='\''){
            if(isdouble){
                current.append(c);
            }else{
                issingle=!issingle;
            }
        }else if(c=='"'){
            if(issingle){
                current.append(c);
            }else{
                isdouble=!isdouble;
            }
        }
        else if(c==' '){
            if(!issingle&&!isdouble){
            if(current.length()>0){
            args.add(current.toString());
            current.setLength(0);
            }
        }else{
            current.append(c);
        }
        }else{
        
            current.append(c);
        }
    }
    
    if(current.length()>0){
        args.add(current.toString());
    
   
}


return args.toArray(new String[0]);
}
        private static Set<String> getAllCommands(List<String> builtins) {
    Set<String> commands = new HashSet<>(builtins);
    String pathEnv = System.getenv("PATH");
    
    if (pathEnv != null) {
        String[] directories = pathEnv.split(":");
        for (String dir : directories) {
            try {
                Path p = Paths.get(dir);
                if (Files.exists(p) && Files.isDirectory(p)) {
                    try (Stream<Path> stream = Files.list(p)) {
                        stream.forEach(path -> {
                           
                            if (Files.isRegularFile(path) && Files.isExecutable(path)) {
                                commands.add(path.getFileName().toString());
                            }
                        });
                    }
                }
            } catch (Exception ignored) {
              
            }
        }
    }
    return commands;
}

private static String getpath(String command){
    String pathenv=System.getenv("PATH");
    if (pathenv == null) 
        return null;
    for(String path:pathenv.split(":")){
        java.nio.file.Path fullpath=java.nio.file.Path.of(path, command);
        if(java.nio.file.Files.exists(fullpath)&&java.nio.file.Files.isExecutable(fullpath)){
            return fullpath.toString();
        }
    }
    return null;
}

private static void runMultiPipeline(List<String> commands, List<String> builtins, InputStream stdin, OutputStream stdout) {
    List<Thread> threads = new ArrayList<>();
    InputStream nextInput = stdin;

    for (int i = 0; i < commands.size(); i++) {
        String command = commands.get(i);
        boolean isLast = (i == commands.size() - 1);

        OutputStream nextOutput;
        InputStream pipeIn = null;

        try {
            if (isLast) {
                nextOutput = stdout;
            } else {
                java.io.PipedOutputStream pos = new java.io.PipedOutputStream();
                pipeIn = new java.io.PipedInputStream(pos);
                nextOutput = pos;
            }

            InputStream threadIn = nextInput;
            OutputStream threadOut = nextOutput;

            Thread t = new Thread(() -> {
                try {
                    handleCommand(command, builtins, threadIn, threadOut);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                
                    if (!isLast && threadOut != System.out) {
                        try { threadOut.close(); } catch (IOException ignored) {}
                    }
                }
            });

            t.start();
            threads.add(t);
            nextInput = pipeIn;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    for (Thread t : threads) {
        try {
            t.join();
        } catch (InterruptedException ignored) {}
    }
}
private static void runExternalPipe(String[] leftArgs, String[] rightArgs, InputStream stdin, OutputStream stdout) throws Exception {
    ProcessBuilder leftPb = new ProcessBuilder(leftArgs);
    leftPb.directory(current.toFile());
    leftPb.redirectError(ProcessBuilder.Redirect.INHERIT);

    ProcessBuilder rightPb = new ProcessBuilder(rightArgs);
    rightPb.directory(current.toFile());
    rightPb.redirectError(ProcessBuilder.Redirect.INHERIT);


    if (stdin == System.in) {
        leftPb.redirectInput(ProcessBuilder.Redirect.INHERIT);
    } else {
        leftPb.redirectInput(ProcessBuilder.Redirect.PIPE);
    }

    rightPb.redirectOutput(ProcessBuilder.Redirect.PIPE);

    List<Process> processes = ProcessBuilder.startPipeline(Arrays.asList(leftPb, rightPb));
    Process leftProc = processes.get(0);
    Process rightProc = processes.get(processes.size() - 1);

   
    if (stdin != System.in) {
        new Thread(() -> {
            try (OutputStream os = leftProc.getOutputStream()) {
                stdin.transferTo(os);
            } catch (IOException ignored) {
            }
        }).start();
    }

   
    try (InputStream is = rightProc.getInputStream()) {
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) {
            stdout.write(buf, 0, n);

         
            for (int i = 0; i < n; i++) {
                if (buf[i] == (byte) '\n') {
                    stdout.flush();
                    break;
                }
            }
        }
        stdout.flush();
    }

    
    rightProc.waitFor();

    if (leftProc.isAlive()) {
        leftProc.destroy();
        leftProc.waitFor(500, TimeUnit.MILLISECONDS);
        if (leftProc.isAlive()) leftProc.destroyForcibly();
    }
}
    }
