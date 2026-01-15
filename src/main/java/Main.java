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
            break;
        }else{
          String path=gethelp(commandtocheck);
          if(path!=null){
            System.out.println(commandtocheck + "is" + path);
          }else{
        System.out.println(input + ": not found");
        }
        } 
        else{
        System.out.println(input + ": command not found");
    }
    }
}

}
private stattic String getpath(String command){
    String pathenv=system.getenv("PATH");
    for(String path:pathenv.split(":")){
        java.nio.file.Path fullpath=java.nio.file.Path(path,command);
        if(java.nio.file.Files.exists(fullpath)&&java.nio.file.Files.isExecutable(fullpath)){
            return fullpath.toString();
        }
    }
    return null;
}
}