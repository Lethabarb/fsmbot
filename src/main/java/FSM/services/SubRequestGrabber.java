// package FSM.services;

// import FSM.entities.Team;
// import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

// public class SubRequestGrabber implements Runnable {
//     private MessageChannel c;
//     private Thread t;
//     public SubRequestGrabber(MessageChannel c) {
//         this.c = c;
//         t = new Thread(this, "SubRequestGrabber");
//         t.start();
//     }

//     @Override
//     public void run() {
//         boolean complete = false;
//         while (!complete) {
//             try {
//                 Thread.sleep(1000);
//             } catch (InterruptedException e) {
//                 // TODO Auto-generated catch block
//                 e.printStackTrace();
//             }
//             System.out.println("Grabber is waiting...");
//             if(Team.getAvailability()) {
//                 System.out.println("Grabber check");
//                 try {
//                     Thread.sleep(1000);
//                 } catch (InterruptedException e) {
//                     // TODO Auto-generated catch block
//                     e.printStackTrace();
//                 }
//                 if (Team.getAvailability()) {
//                     System.out.println("Grabber start");
//                     DiscordBot bot = DiscordBot.getInstance();
//                     bot.createSubReqestsFromChannel(c);
//                     complete = true;
//                 }
//             }

//         }
//         // TODO Auto-generated method stub
//         // throw new UnsupportedOperationException("Unimplemented method 'run'");
//     }

//     public MessageChannel getC() {
//         return c;
//     }

//     public void setC(MessageChannel c) {
//         this.c = c;
//     }
    
// }
