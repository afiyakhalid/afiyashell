import java.util.Arrays;
import java.util.List;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        // TODO: Uncomment the code below to pass the first stage
       
        Scanner scanner=new Scanner(System.in);
        List<String> builtins=Arrays.asList("echo","exit","type");
        
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
       } else{
        
        String [] parts=input.split(" ");
        String command=parts[0];
        String commandpath=getpath(command);
        if(commandpath!=null){
            ProcessBuilder pb=new ProcessBuilder(parts);
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
