import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;  // <--- Added this just to be safe
import java.util.List;
import java.util.Scanner;  // <--- Added this just to be safe


public class Main {
    private static Path current=Paths.get(System.getProperty("user.dir"));
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
       
        Scanner scanner=new Scanner(System.in);
        List<String> builtins=Arrays.asList("echo","exit","type","pwd");
        
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
           String message=input.substring(5);
           System.out.println(message);
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
            Path newpath=current.resolve(pathstring).normalize();
            if(Files.isDirectory(newpath)){
                current=newpath;
            }else{
                System.out.println("cd: " + pathstring + ": No such file or directory");
            }
        }else{
        
        String [] parts=input.split(" ");
        String command=parts[0];
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
