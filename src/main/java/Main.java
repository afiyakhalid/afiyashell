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
        else if(input.startsWith("type")){
        String commandtocheck=input.substring(5);
        if(builtins.contains(commandtocheck))
        {
            System.out.println(commandtocheck+" :  is a shell built in");
        }else{
            System.out.println(commandtocheck+ " :not found");
        }
        } 
        else{
        System.out.println(input + ": command not found");
    }
    }
}
}