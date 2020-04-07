import java.io.*;
import java.util.*;

public class Main {

    static List<Process> IO_Queue = new ArrayList<>();
    static List<Process> processes = new ArrayList<>();
    static List<Process> completed = new LinkedList<Process>();
    static int time = 0;

    public static void main(String[] args) {

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    "proc_info"));
            String line = reader.readLine();
            int index = 1;
            while (line != null) {
                List<Integer> execs = new ArrayList<>();
                for (String s : line.split(",")) {
                    execs.add(Integer.parseInt(s));
                }

                Process p = new Process(execs, index);
                p.putToWait(time);
                processes.add(p);
                index++;

                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Process current_process = getNextReady();
        Process next = null;
        current_process.putToCPU(time);
        while (completed.size() != processes.size()) {
            boolean shift = false;
            for (Process p : processes) {

                if(p.is_io && !p.finished) {
                    if (p.currentExecTime==0){
                        p.putToWait(time);
                        if(allBusy()){
                            current_process = p;
                            p.putToCPU(time);
                            shift = false;
                        }
                    }
                }

                p.updateTime(time);

                if(current_process.currentExecTime==0 && !current_process.is_wait && !current_process.is_io && !current_process.finished){
                    shift = true;
                }

            }


            time++;


            if(shift){
                /*
                for (Process p:
                        processes) {
                    p.order--;
                }*/
                if(current_process.exec_times.size()!=0 && !current_process.finished){
                    current_process.putToIO(time);
                } else {
                    if(!current_process.finished){
                        System.out.println(current_process.name+" Finished at Time: " + time);
                        current_process.completion_time = time;
                        current_process.finished = true;
                        current_process.order = 3000;
                        completed.add(current_process);
                    }
                }


                current_process = getNextReady();


                if(current_process.exec_times.size()!=0 && current_process.is_wait) {
                    current_process.putToCPU(time);
                }
            }


        }
        System.out.println("Total Time: " + time);
        System.out.println("ID\tSTART\tCPU\t\tIO\t\tWAIT\tEND");
        for (Process p: processes){
            System.out.println(p.name+"\t"+p.start+"\t\t"+p.cpu_time + "\t\t" + p.io_time + "\t\t" + p.wait_time+"\t\t"+p.completion_time);
        }
        double total_wait = 0;
        double total_cpu = 0;
        double total_start = 0;
        double total_end = 0;

        for (Process p: processes){
            total_cpu += p.cpu_time;
            total_end += p.completion_time;
            total_start += p.start;
            total_wait += p.wait_time;
        }
        double avg_wait = total_wait/8.0;
        double avg_cpu = total_cpu/(time*1.0)*100.0;
        double avg_start = total_start/8.0;
        double avg_end = total_end/8.0;
        System.out.println("CPU Utilization: " + avg_cpu);
        System.out.println("Tr\t\tTw\t\tTtr");
        System.out.println(avg_start+"\t"+ avg_wait +"\t"+avg_end);
    }

    public static boolean allBusy(){
        return IO_Queue.size()==processes.size()-completed.size()-1;
    }


    public static Process getNextReady(){
        Process next = processes.get(0);
        for (Process p : processes) {
            if (p.order < next.order && p.is_wait) {
                next = p;
            }
        }
        return next;

    }
}

class Process {

    String name = "";
    boolean is_io = false;
    boolean is_wait = true;
    boolean finished = false;
    public List<Integer> exec_times;
    int order = 0;
    int currentExecTime = 0;
    int completion_time = 0;
    int wait_time = 0;
    int cpu_time = 0;
    int io_time = 0;
    int start = -1;

    public Process(List<Integer> executions, int index) {
        exec_times = executions;
        this.name = "P" + index;
        order = index;
    }

    public void putToWait(int time) {
        System.out.println(name + " In Wait now at Time: " + time);
        is_io = false;
        is_wait = true;
        Main.IO_Queue.remove(this);
        this.order = exec_times.get(0);
            /*Main.processes.remove(this);
            Main.processes.add(this);*/
    }

    public void putToIO(int time) {
        System.out.println(name + " In IO now at Time: " + time);
        is_io = true;
        is_wait = false;
        currentExecTime = exec_times.remove(0);
        order = 1000 + order;
        //Main.processes.add(Main.processes.remove());
        Main.IO_Queue.add(this);
    }

    public void putToCPU(int time) {
        System.out.println(name + " In CPU now at Time: " + time);
        is_io = false;
        is_wait = false;
        currentExecTime = exec_times.remove(0);
    }

    public void updateTime(int time) {
        if(finished) return;
        if (is_io) {
            io_time++;
            currentExecTime--;
        } else if (is_wait) {
            wait_time++;
            //currentExecTime = 0;
        } else {
            if (start == -1) {
                start = time;
                System.out.println(name + " Started at Time: " + time);
            }
            cpu_time++;
            currentExecTime--;
        }
    }
}

