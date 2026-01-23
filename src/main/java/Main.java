import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;  
import java.util.List;
import java.util.Scanner;


public class Main {
    private static Path current=Paths.get(System.getProperty("user.dir"));
    public static void main(String[] args) throws Exception {
   
       
      
        List<String> builtins=Arrays.asList("echo","exit","type","pwd","cd");
        if (System.console() == null) {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                System.out.print("$ ");
                System.out.flush();

                String input = scanner.nextLine().trim();
                if (input.equals("exit")) {
                    break; 
                }

                handleCommand(input, builtins);
            }
            return;
        }
        
        while(true){
               System.out.print("$ ");
            System.out.flush();
             setRawMode(true);
             StringBuilder inputbuffer=new StringBuilder();
             while(true){
                int c=System.in.read();
                if(c==9){
                    String currentinput=inputbuffer.toString();
                    List<String> candidates=new ArrayList<>();
                    if("echo".startsWith(currentinput)){
                        candidates.add("echo");
                    }
                    if("exit".startsWith(currentinput)){
                        candidates.add("exit");
                    }
                    if(candidates.size()==1){
                        String matches=candidates.get(0);
                        String suffix=matches.substring(inputbuffer.length()) + " ";
                        System.out.print(suffix);
                        inputbuffer.append(suffix);
                    

                    }
                }else if (c==127) {
                   if(inputbuffer.length()>0){
                    inputbuffer.deleteCharAt(inputbuffer.length()-1);
                    System.out.print("\b \b");
                   }
                }else if(c==10||c==13){
                    System.out.print("\r\n");
                   
                    break;

                }
                else{
                    char ch=(char)c;
                       System.out.print(ch);
                    inputbuffer.append(ch);
                    
                }
             }
             setRawMode(false);
                String input = inputbuffer.toString().trim();
            if (input.equals("exit")) {
                break; 
            }

            handleCommand(input, builtins);
        }
    }
        

   private static void handleCommand(String input, List<String> builtins) throws Exception {
        if (input.isEmpty()) return;
       
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
                System.out.print(argstoadd.get(j));
                if(j<argstoadd.size()-1){
                    System.out.print(" ");
                }
            }
            System.out.println();
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
            System.out.println(commandtocheck +" is a shell builtin");
           

        }else{
          String path=getpath(commandtocheck);
          if(path!=null){
            System.out.println(commandtocheck + " is " + path);
          }else{
        System.out.println(commandtocheck + ": not found");
        }
        } 
       }else if(input.equals("pwd")) {
      
        System.out.println(current.toString());
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
                System.out.println("cd: " + pathstring + ": No such file or directory");
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

                        if (outputfile != null) {
                            if(append){
                            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(outputfile));
                             } else{
                           pb.redirectOutput(outputfile);
                             }
                        } else {
                            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
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
                        process.waitFor();
                    } else {
                        System.out.println(command + ": not found");
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
}


    


