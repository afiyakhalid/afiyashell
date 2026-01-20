import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;  // <--- Added this just to be safe
import java.util.List;
import java.util.Scanner;  // <--- Added this just to be safe


public class Main {
    private static Path current=Paths.get(System.getProperty("user.dir"));
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
       
        Scanner scanner=new Scanner(System.in);
        List<String> builtins=Arrays.asList("echo","exit","type","pwd","cd");
        
        while(true){
               System.out.print("$ ");
            System.out.flush();
        String input=scanner.nextLine();
   
        if(input.equals("exit"))
        {
            System.out.println(input);
            break;
    }
    else if (input.startsWith("echo "))
        {
        String[] parts=parseArguments(input);
           for (int i = 1; i < parts.length; i++) {
                    System.out.print(parts[i]);
                    // Add space only if it's not the last word
                    if (i < parts.length - 1) {
                        System.out.print(" ");
                    }
                }
                System.out.println();
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
            String command = parts[0];
        String commandpath=getpath(command);
        if(commandpath!=null){
            ProcessBuilder pb=new ProcessBuilder(parts);
            pb.directory(current.toFile());
            pb.inheritIO();
            Process process=pb.start();
            process.waitFor();


        }else{
            System.out.println(input + ": command not found");
        }
    }
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

