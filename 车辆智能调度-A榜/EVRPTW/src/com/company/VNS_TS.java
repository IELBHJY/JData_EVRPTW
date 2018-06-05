package com.company;


import java.util.*;

public class VNS_TS {
    final int size=10000;
    Data data;
    List<List<Integer>>[] solutions;
    double[] objections;
    HashMap<Integer,Date>[] times;
    public VNS_TS() {
        objections=new double[size];
    }

    private void readData(){
        data=new Data();
        data.readTransportionData();
        data.readDistanceTime();
        data.readVehicle();
    }

    public void creatFirstSolution() {
        readData();
        System.out.println("data done");
        solutions = new List[size];
        Queue<Integer> queue;
        int count = data.shops.keySet().size();
        for (int i = 0; i < size; i++) {
            final int num = 1 + (int) (Math.random() * (count));//随机选择一个点
             queue = new PriorityQueue<Integer>(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Double.compare(calAngel(data.shops.get(num), data.shops.get(o1)),
                            calAngel(data.shops.get(num), data.shops.get(o2)));
                }
            });
            for (int j = 1; j <= count; j++) {
                queue.offer(j);
            }
            solutions[i] = new ArrayList<List<Integer>>();
            double weights = 0.0;
            double volumes = 0.0;
            double distance=0.0;
            List<Integer> list = new ArrayList<Integer>();
            list.add(num);
            while(queue.size()>900) {
                int j=queue.poll();
                weights += data.shops.get(j).pack_total_weight;
                volumes += data.shops.get(j).getPack_total_volume;
                if(list.size()==0) distance+=data.distances[0][j];
                distance+=data.distances[list.size()-1][j];
                if (weights <= data.vehicles.get(2).max_weight && volumes <= data.vehicles.get(2).max_volume) {
                    if(distance>data.vehicles.get(2).driving_range){
                        solutions[i].add(list);
                        list = new ArrayList<Integer>();
                        list.add(j);
                        weights = 0.0;
                        volumes = 0.0;
                        distance=0.0;
                        weights += data.shops.get(j).pack_total_weight;
                        volumes += data.shops.get(j).getPack_total_volume;
                        distance+=data.distances[0][j];
                    }else {
                        int size = list.size();
                        int k;
                        for (k = 0; k < size; k++) {
                            if (data.shops.get(j).first_receive_tm.before(data.shops.get(list.get(k)).first_receive_tm)) {
                                if(k==0){
                                    Calendar calendar=new GregorianCalendar(2018,3,9,8,0);
                                    calendar.add(Calendar.MINUTE,data.times[0][j]);
                                    Date date=calendar.getTime();
                                    Date early=data.shops.get(j).first_receive_tm;
                                    if (date.before(early)) {
                                        calendar.setTime(early);
                                    } else {
                                        calendar.setTime(date);
                                    }
                                    calendar.add(Calendar.MINUTE,data.times[j][list.get(k)]);
                                    date=calendar.getTime();
                                    Date late=data.shops.get(list.get(k)).last_receive_tm;
                                    if(late.after(date)) list.add(k, j);
                                }else{
                                    list.add(k, j);
                                }
                                break;
                            }
                        }
                        if (k == size) {
                            list.add(j);
                        }
                    }
                } else {
                    solutions[i].add(list);
                    list = new ArrayList<Integer>();
                    list.add(j);
                    weights = 0.0;
                    volumes = 0.0;
                    distance=0.0;
                    weights += data.shops.get(j).pack_total_weight;
                    volumes += data.shops.get(j).getPack_total_volume;
                    distance+=data.distances[0][j];
                }
            }
        }
        System.out.println("done");
    }

    public void creatFirstSolution1() {
        readData();
        System.out.println("data done");
        solutions = new List[size];
        Queue<Integer> queue;
        int count = data.shops.keySet().size();
        for (int i = 0; i < size; i++) {
            queue=new LinkedList<Integer>();
            int num = 1 + (int) (Math.random() * (count));
            for(int j=num;j<=1000;j++){
                queue.offer(j);
            }
            for(int j=1;j<num;j++){
                queue.offer(j);
            }
            solutions[i] = new ArrayList<List<Integer>>();
            double weights = 0.0;
            double volumes = 0.0;
            List<Integer> list = new ArrayList<Integer>();
            list.add(num);
            while(!queue.isEmpty()) {
                int j=queue.poll();
                weights += data.shops.get(j).pack_total_weight;
                volumes += data.shops.get(j).getPack_total_volume;
                if (weights <= data.vehicles.get(1).max_weight && volumes <= data.vehicles.get(1).max_volume) {
                    int size=list.size();
                    int k;
                    for(k=0;k<size;k++){
                        if(data.shops.get(j).last_receive_tm.before(data.shops.get(list.get(k)).last_receive_tm)){
                            list.add(k,j);
                            break;
                        }
                    }
                    if(k==size){
                        list.add(j);
                    }
                } else {
                    solutions[i].add(list);
                    list = new ArrayList<Integer>();
                    list.add(j);
                    weights = 0.0;
                    volumes = 0.0;
                    weights += data.shops.get(j).pack_total_weight;
                    volumes += data.shops.get(j).getPack_total_volume;
                }
            }
        }
        System.out.println("done");
    }

    public void creatFirstSolution2() {
        readData();
        System.out.println("data done");
        solutions = new List[size];
        Queue<Integer> queue;
        int count = data.shops.keySet().size();
        for (int i = 0; i < size; i++) {
            queue = new PriorityQueue<Integer>(new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return Double.compare(data.shops.get(o1).last_receive_tm.getTime(),
                            data.shops.get(o2).last_receive_tm.getTime());
                }
            });
            for (int j = 1; j <= count; j++) {
                queue.offer(j);
            }
            solutions[i] = new ArrayList<List<Integer>>();
            double weights = 0.0;
            double volumes = 0.0;
            Date date;
            Date early;
            Date late;
            Calendar calendar=new GregorianCalendar(2018,3,9,8,0);
            List<Integer> list = new ArrayList<Integer>();
            while(!queue.isEmpty()) {
                int j=queue.poll();
                if(list.size()==0){
                    list.add(j);
                    weights+=data.shops.get(j).pack_total_weight;
                    volumes+=data.shops.get(j).getPack_total_volume;
                    calendar.add(Calendar.MINUTE,data.times[0][j]);
                    date=calendar.getTime();
                    early=data.shops.get(j).first_receive_tm;
                    if(date.before(early)){
                        calendar.setTime(early);
                        calendar.add(Calendar.MINUTE,30);
                    }else{
                        calendar.setTime(date);
                        calendar.add(Calendar.MINUTE,30);
                    }
                }else{
                    if(weights+data.shops.get(j).pack_total_weight<=data.vehicles.get(1).max_weight
                            && volumes+data.shops.get(j).getPack_total_volume<=data.vehicles.get(1).max_volume){
                        int size=list.size();
                        calendar.add(Calendar.MINUTE,data.times[list.get(size-1)][j]);
                        date=calendar.getTime();
                        early=data.shops.get(j).first_receive_tm;
                        late=data.shops.get(j).last_receive_tm;
                        if(late.after(date)){
                            list.add(j);
                            weights+=data.shops.get(j).pack_total_weight;
                            volumes+=data.shops.get(j).getPack_total_volume;
                            if(date.before(early)){
                                calendar.setTime(early);
                                calendar.add(Calendar.MINUTE,30);
                            }else{
                                calendar.setTime(date);
                                calendar.add(Calendar.MINUTE,30);
                            }
                        }else{
                            solutions[i].add(list);
                            list=new ArrayList<Integer>();
                            list.add(j);
                            calendar=new GregorianCalendar(2018,3,9,8,0);
                            weights+=data.shops.get(j).pack_total_weight;
                            volumes+=data.shops.get(j).getPack_total_volume;
                            calendar.add(Calendar.MINUTE,data.times[0][j]);
                            date=calendar.getTime();
                            early=data.shops.get(j).first_receive_tm;
                            if(date.before(early)){
                                calendar.setTime(early);
                                calendar.add(Calendar.MINUTE,30);
                            }else{
                                calendar.setTime(date);
                                calendar.add(Calendar.MINUTE,30);
                            }
                        }
                    }else{
                        solutions[i].add(list);
                        list=new ArrayList<Integer>();
                        list.add(j);
                        calendar=new GregorianCalendar(2018,3,9,8,0);
                        weights+=data.shops.get(j).pack_total_weight;
                        volumes+=data.shops.get(j).getPack_total_volume;
                        calendar.add(Calendar.MINUTE,data.times[0][j]);
                        date=calendar.getTime();
                        early=data.shops.get(j).first_receive_tm;
                        if(date.before(early)){
                            calendar.setTime(early);
                            calendar.add(Calendar.MINUTE,30);
                        }else{
                            calendar.setTime(date);
                            calendar.add(Calendar.MINUTE,30);
                        }
                    }
                }
            }
        }
        System.out.println("done");
    }


    public double calAngel(Node p1,Node p2){
        double res;
        double k=(data.lng-p1.lng)/(data.lat-p1.lat);
        double b=data.lng-k*data.lat;
        if(k*p2.lat+b>=p2.lng){
            double e1=Math.sqrt(Math.pow(data.lng-p1.lng,2)+Math.pow(data.lat-p1.lat,2));
            double e2=Math.sqrt(Math.pow(data.lng-p2.lng,2)+Math.pow(data.lat-p2.lat,2));
            double e3=Math.sqrt(Math.pow(p2.lng-p1.lng,2)+Math.pow(p2.lat-p1.lat,2));
            double cos=(Math.pow(e1,2)+Math.pow(e2,2)-Math.pow(e3,2))/(2*e1*e2);
            res=Math.acos(cos);
        }else{
            double e1=Math.sqrt(Math.pow(data.lng-p1.lng,2)+Math.pow(data.lat-p1.lat,2));
            double e2=Math.sqrt(Math.pow(data.lng-p2.lng,2)+Math.pow(data.lat-p2.lat,2));
            double e3=Math.sqrt(Math.pow(p2.lng-p1.lng,2)+Math.pow(p2.lat-p1.lat,2));
            double cos=(Math.pow(e1,2)+Math.pow(e2,2)-Math.pow(e3,2))/(2*e1*e2);
            res=2*Math.PI-Math.acos(cos);
        }
        return res;
    }

    private void calObjection(){
        int count=0;
        times=new HashMap[size];
        for(int s=0;s<size;s++) {
            List<List<Integer>> solution = solutions[s];
            times[s]=new HashMap<Integer, Date>();
            double res = 0.0;
            double pentyTimes=0.0;
            //考虑电量约束，判断时间窗是否满足
            for(int i=0;i<solution.size();i++){
                Calendar calendar=new GregorianCalendar(2018,3,9,8,0);
                List<Integer> list=solution.get(i);
                List<Date> time=new ArrayList<Date>();
                int size=list.size();
                if(size==0) continue;
                int first=list.get(0);
                calendar.add(Calendar.MINUTE,data.times[0][first]);
                Date date = calendar.getTime();
                Date late=data.shops.get(first).last_receive_tm;
                Date early=data.shops.get(first).first_receive_tm;
                if(late.after(date)){
                  if(date.before(early)){
                      time.add(early);
                  }else{
                      time.add(date);
                  }
                  //times[s].put(first,time.get(0));
                  calendar.setTime(time.get(0));
                  calendar.add(Calendar.MINUTE,30);
                }else{
                    objections[s]=Double.MAX_VALUE;
                    System.exit(0);
                    System.out.println("0-----objections[s]=Double.MAX_VALUE");
                    break;
                }
                for(int j=1;j<size;j++){
                    calendar.add(Calendar.MINUTE,data.times[list.get(j-1)][list.get(j)]);
                    date=calendar.getTime();
                    late=data.shops.get(list.get(j)).last_receive_tm;
                    early=data.shops.get(list.get(j)).first_receive_tm;
                    if(late.after(date)){
                        if(date.before(early)){
                            time.add(early);
                        }else{
                            time.add(date);
                        }
                        //times[s].put(list.get(j),time.get(j));
                        calendar.setTime(time.get(j));
                        calendar.add(Calendar.MINUTE,30);
                    }else{
                        pentyTimes+=date.getTime()-late.getTime();
                        break;
                    }
                }
            }
            if(pentyTimes<1){
                count++;
            }
            objections[s] = pentyTimes;
        }
        System.out.println("初始种群中有"+count+"个个体。");
    }


    public void update(){
        creatFirstSolution();
        calObjection();
        double min=objections[0];
        int label=0;
        for(int i=1;i<size;i++){
            if(objections[i]<min) {
                min = objections[i];
                label=i;
            }
        }
        System.out.println("目标函数值："+min);
        System.out.println(solutions[label].size());
        List<List<Integer>> solution=solutions[label];
        for(List list:solution){
            for(int i=0;i<list.size();i++){
                System.out.print(list.get(i)+" ");
            }
            System.out.println();
        }
    }
}
