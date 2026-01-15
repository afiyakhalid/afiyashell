// mport java.util.Scanner;
// import java.util.List;
// import java.util.Arrays;
// import java.nio.file.Files; // Import these to make lines shorter
// import java.nio.file.Path;
// import java.nio.file.Paths;


// public class Main {
//     public static void main(String[] args) throws Exception {
//         // TODO: Uncomment the code below to pass the first stage
       
//         Scanner scanner=new Scanner(System.in);
//         List<String> builtins=Arrays.asList("echo","exit","type");
        
//         while(true){
//                System.out.print("$ ");
//             System.out.flush();
//         String input=scanner.nextLine();
   
//         if(input.equals("exit"))
//         {
//             System.out.println(input);
//             break;
//     }
//     else if (input.startsWith("echo "))
//         {
//            String message=input.substring(5);
//            System.out.println(message);
//         }
//                 else if (input.startsWith("type ")) {
//                 String commandtocheck = input.substring(5);
//         if(builtins.contains(commandtocheck))
//         {
//             System.out.println(commandtocheck +" is a shell builtin");
           

//         }else{
//           String path=getpath(commandtocheck);
//           if(path!=null){
//             System.out.println(commandtocheck + " is " + path);
//           }else{
//         System.out.println(input + ": not found");
//         }
//         } 
//        } else{
//         System.out.println(input + ": command not found");
//     }
//     }
// }


// private static String getpath(String command){
//     String pathenv=System.getenv("PATH");
//     if (pathenv == null) 
//         return null;
//     for(String path:pathenv.split(":")){
//         java.nio.file.Path fullpath=java.nio.file.Path.of(path, command)
//         if(java.nio.file.Files.exists(fullpath)&&java.nio.file.Files.isExecutable(fullpath)){
//             return fullpath.toString();
//         }
//     }
//     return null;
// }
// }
import java.util.Scanner;
import java.util.List;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main { // <--- Class starts here
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        List<String> builtins = Arrays.asList("echo", "exit", "type");
        
        while (true) {
            System.out.print("$ ");
            System.out.flush();
            String input = scanner.nextLine();
   
            // FIX 1: Use "exit 0" and remove the print statement
            if (input.equals("exit ")) {
                break;
            }
            else if (input.startsWith("echo ")) {
                String message = input.substring(5);
                System.out.println(message);
            }
            else if (input.startsWith("type ")) {
                String commandtocheck = input.substring(5);
                
                if (builtins.contains(commandtocheck)) {
                    System.out.println(commandtocheck + " is a shell builtin");
                    // FIX 2: Removed "break" here so the shell keeps running!
                } else {
                    // FIX 3: Changed 'gethelp' to 'getpath'
                    String path = getpath(commandtocheck);
                    
                    if (path != null) {
                        // FIX 4: Added spaces: " is "
                        System.out.println(commandtocheck + " is " + path);
                    } else {
                        System.out.println(commandtocheck + ": not found");
                    }
                } 
            } 
            else {
                System.out.println(input + ": command not found");
            }
        }
    } // <--- Main method ends here

    // FIX 5: This function is now INSIDE the Main class
    private static String getpath(String command) {
        String pathenv = System.getenv("PATH");
        if (pathenv == null) return null;
        
        for (String path : pathenv.split(":")) {
            // FIX 6: Used Path.of(...) instead of invalid Path(...) syntax
            Path fullpath = Path.of(path, command);
            
            if (Files.exists(fullpath) && Files.isExecutable(fullpath)) {
                return fullpath.toString();
            }
        }
        return null;
    }

} // <--- Class ends here