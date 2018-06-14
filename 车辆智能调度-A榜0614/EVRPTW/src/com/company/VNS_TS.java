package com.company;


import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;

public class VNS_TS {
    final int size=80000;
    final int batch=10;
    Data data;
    HashMap<Integer,List<Integer>>[] solutions;
    HashMap<Integer,List<Integer>> results;
    HashMap<Integer,Date> resultTimes;
    HashMap<Integer,Double> allVechileWaitTime;
    int vechSum=1;
    int vechSum1=1;
    int vechSum2=1;
    int vechSum3=1;
    int vechSum4=1;
    int label;
    double[] objections;
    HashMap<Integer,Date>[] times;
    //HashMap<Integer,Long>[] waitTimes;
    HashMap<Integer,Double>[] waitTimes;
    int vech_type=2;
    int[][] taskSolutions=new int[size][batch+1];
    HashMap<Integer,Double>[] costs;
    HashMap<Integer,Double>[] distances;
    List<Integer> priorList;
    Queue<Integer> queue;
    double sums=0.0;
    int start;
    public VNS_TS() {
        objections=new double[size];
        readData();
        System.out.println("data done");
    }
    private void readData(){
        data=new Data();
        data.readTransportionData();
        data.readDistanceTime();
        data.readVehicle();
        solutions = new HashMap[size];
        int count = data.shops.keySet().size();
        final int startPoint = 1 + (int) (Math.random() * (count));//随机选择一个点,该点可作为函数参数输入
        queue = new PriorityQueue<Integer>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return Double.compare(calAngel(data.shops.get(startPoint), data.shops.get(o1)),
                        calAngel(data.shops.get(startPoint), data.shops.get(o2)));
            }
        });
        for (int j = 1; j <= count; j++) {
            queue.offer(j);
        }
        priorList=new ArrayList<Integer>();
        for(int i=0;i<count;i++){
            priorList.add(queue.poll());
        }
        results=new HashMap<Integer, List<Integer>>();
        resultTimes=new HashMap<Integer, Date>();
        allVechileWaitTime=new HashMap<Integer, Double>();
    }

    public void creatFirstSolution1(int startPoint) {
        for (int i = 0; i < size; i++) {
            solutions[i]=new HashMap<Integer, List<Integer>>();
            List<Integer> tasks=new ArrayList<Integer>();
            for(int k=startPoint;k<startPoint+batch;k++){
                tasks.add(priorList.get(k));
            }
            List<Integer> list=new ArrayList<Integer>();
            for(int j=1;j<=batch;j++){
                int num=(int) (Math.random() * (batch));
                while(list.contains(num)){
                    num=(int) (Math.random() * (batch));
                }
                taskSolutions[i][j]=tasks.get(num);
                list.add(num);
            }
            double weight=0.0,volume=0.0,distance=0.0;
            list=new ArrayList<Integer>();
            int vech=1;
            for(int j=1;j<=batch;j++){
                if(weight+data.shops.get(taskSolutions[i][j]).pack_total_weight<=data.vehicles.get(vech_type).max_weight&&
                        volume+data.shops.get(taskSolutions[i][j]).Pack_total_volume<=data.vehicles.get(vech_type).max_volume){
                    int size=list.size();
                    if(size==0) {
                        list.add(taskSolutions[i][j]);
                        weight+=data.shops.get(taskSolutions[i][j]).pack_total_weight;
                        volume+=data.shops.get(taskSolutions[i][j]).Pack_total_volume;
                        distance+=data.distances[0][taskSolutions[i][j]];
                    }else{
                        if(distance+data.distances[taskSolutions[i][j-1]][taskSolutions[i][j]]<=data.vehicles.get(vech_type).driving_range){
                                //如果加入当前点后可以顺利到充电站，则加入，否则不加入。
                                if(distance+data.distances[taskSolutions[i][j-1]][taskSolutions[i][j]]+
                                        data.distances[taskSolutions[i][j]][calNearestRecharge(taskSolutions[i][j])]<=data.vehicles.get(vech_type).driving_range) {
                                    weight += data.shops.get(taskSolutions[i][j]).pack_total_weight;
                                    volume += data.shops.get(taskSolutions[i][j]).Pack_total_volume;
                                    distance += data.distances[taskSolutions[i][j - 1]][taskSolutions[i][j]];
                                    list.add(taskSolutions[i][j]);
                                }else{
                                    solutions[i].put(vech++,list);
                                    list=new ArrayList<Integer>();
                                    list.add(taskSolutions[i][j]);
                                    weight=0.0;
                                    volume=0.0;
                                    distance=0.0;
                                    weight+=data.shops.get(taskSolutions[i][j]).pack_total_weight;
                                    volume+=data.shops.get(taskSolutions[i][j]).Pack_total_volume;
                                    distance+=data.distances[0][taskSolutions[i][j]];
                                }
                        }else{
                            solutions[i].put(vech++,list);
                            list=new ArrayList<Integer>();
                            list.add(taskSolutions[i][j]);
                            weight=0.0;
                            volume=0.0;
                            distance=0.0;
                            weight+=data.shops.get(taskSolutions[i][j]).pack_total_weight;
                            volume+=data.shops.get(taskSolutions[i][j]).Pack_total_volume;
                            distance+=data.distances[0][taskSolutions[i][j]];
                        }
                    }
                }else{
                    solutions[i].put(vech++,list);
                    list=new ArrayList<Integer>();
                    list.add(taskSolutions[i][j]);
                    weight=0.0;
                    volume=0.0;
                    distance=0.0;
                    weight+=data.shops.get(taskSolutions[i][j]).pack_total_weight;
                    volume+=data.shops.get(taskSolutions[i][j]).Pack_total_volume;
                    distance+=data.distances[0][taskSolutions[i][j]];
                }
            }
            solutions[i].put(vech++,list);
        }
    }

   /* private void calObjection1(){
        int count=0;
        double cost=data.vehicles.get(2).unit_trans_cost/1000.0;
        times=new HashMap[size];
        waitTimes=new HashMap[size];
        costs=new HashMap[size];
        //costss=new double[size][3];
        for(int s=0;s<size;s++) {
            long waitTime=0;//计算等待时间
            HashMap<Integer,List<Integer>> solution = solutions[s];
            times[s]=new HashMap<Integer, Date>();
            waitTimes[s]=new HashMap<Integer, Long>();
            costs[s]=new HashMap<Integer, Double>();
            List<Double> b=new ArrayList<Double>();
            double pentyTimes=0.0;
            double res=0.0;
            //考虑电量约束，判断时间窗是否满足
            for(int i=1;i<=solution.keySet().size();i++){
                Calendar calendar=new GregorianCalendar(2018,3,9,8,0);
                List<Integer> list=solution.get(i);
                int size=list.size();
                if(size==0) continue;
                int first=list.get(0);
                double distance=0.0;
                calendar.add(Calendar.MINUTE,data.times[0][first]);
                Date date = calendar.getTime();
                Date late=data.shops.get(first).last_receive_tm;
                Date early=data.shops.get(first).first_receive_tm;
                if(late.after(date)){
                    if(date.before(early)){
                        times[s].put(first,early);
                    }else{
                        times[s].put(first,date);
                    }
                    calendar.setTime(times[s].get(first));
                    res+=data.distances[0][first]*cost;
                    distance+=data.distances[0][first];
                    if(distance>data.vehicles.get(vech_type).driving_range){
                        System.out.println("第一个任务不满足distance");
                    }
                    calendar.add(Calendar.MINUTE,30);//添加服务时间30分钟
                }else{
                    objections[s]=Double.MAX_VALUE;
                    System.exit(0);
                    System.out.println("160---objections[s]=Double.MAX_VALUE");
                    break;
                }
                for(int j=1;j<size;j++){
                    calendar.add(Calendar.MINUTE,data.times[list.get(j-1)][list.get(j)]);
                    date=calendar.getTime();
                    late=data.shops.get(list.get(j)).last_receive_tm;
                    early=data.shops.get(list.get(j)).first_receive_tm;
                    if(!late.before(date)){
                        if(date.before(early)){
                            times[s].put(list.get(j),early);
                            waitTime+=early.getTime()-date.getTime();
                        }else{
                            times[s].put(list.get(j),date);
                        }
                        calendar.setTime(times[s].get(list.get(j)));
                        res+=data.distances[list.get(j-1)][list.get(j)]*cost;
                        distance+=data.distances[list.get(j-1)][list.get(j)];
                        if(distance>data.vehicles.get(vech_type).driving_range){
                            System.out.println("179-----中间任务不满足distance");
                            System.exit(0);
                        }
                        calendar.add(Calendar.MINUTE,30);
                    }else{
                        pentyTimes+=10000+date.getTime()-late.getTime();//除以1000是秒
                        times[s].put(list.get(j),date);
                        calendar.setTime(date);
                        res+=data.distances[list.get(j-1)][list.get(j)]*cost;
                        distance+=data.distances[list.get(j-1)][list.get(j)];
                        if(distance>data.vehicles.get(vech_type).driving_range){
                            System.out.println("190----中间任务不满足distance");
                            System.exit(0);
                        }
                        calendar.add(Calendar.MINUTE,30);
                    }
                }
                int last=list.get(size-1);
                if(distance+data.distances[0][last]>data.vehicles.get(vech_type).driving_range){
                    int nearestR=calNearestRecharge(last);
                    res+=data.distances[last][nearestR]*cost+data.distances[nearestR][0]*cost;
                    res+=50;
                    list.add(nearestR);
                    solutions[s].put(i,list);
                }else{
                    res+=data.distances[0][last]*cost;
                }
                if(b.size()==0) {
                    b.add(res);
                }
                else{
                    b.add(res-b.get(b.size()-1));
                }
                long thistime=0;
                for(Long key:waitTimes[s].values()){
                    thistime=waitTime-key;
                }
                waitTimes[s].put(i,thistime);
                double temp=b.get(b.size()-1);
                costs[s].put(i,temp);
            }
            if(pentyTimes<0.00001){
                count++;
            }
            int sum=solutions[s].keySet().size();
            double wait_cost=24*waitTime/(1000.0*60*60);
            objections[s] = res+300*sum+wait_cost+pentyTimes;
        }
        if(count==0){
            creatFirstSolution1(start);
            calObjection1();
        }
    }*/

    private void calObjection2(){
        int count=0;
        times=new HashMap[size];
        waitTimes=new HashMap[size];
        costs=new HashMap[size];
        distances=new HashMap[size];
        for(int s=0;s<size;s++) {
            HashMap<Integer,List<Integer>> solution = solutions[s];
            times[s]=new HashMap<Integer, Date>();
            waitTimes[s]=new HashMap<Integer, Double>();
            costs[s]=new HashMap<Integer, Double>();
            distances[s]=new HashMap<Integer, Double>();
            double res=0.0;
            double distance=0.0;
            double waitTime=0.0;
            double pentytime=0.0;
            double d=0.0;
            double w=0.0;
            double r=0.0;
            //考虑电量约束，判断时间窗是否满足
            for(int i=1;i<=solution.keySet().size();i++){
                d=0.0;
                w=0.0;
                r=0.0;
                List<Integer> list=solution.get(i);
                double[] temp=calTaskTimes(list,s);
                w=temp[0];
                double w1=calWaitTimes(list);
                if(w!=w1){
                    System.out.println("291---"+w+" "+w1);
                    System.exit(0);
                }
                waitTime+=temp[0];
                pentytime+=temp[1];
                double[] ans=calDistance(list,s,i);
                d=ans[0];
                r=ans[1];
                distance+=ans[0];
                res+=ans[1];
                //存储每个车的行驶距离成本，等待时间成本
                waitTimes[s].put(vechSum2++,w);
                costs[s].put(vechSum3++,r);
                distances[s].put(vechSum4++,d);
            }
            if(pentytime<0.001){
                count++;
            }
            int sum=solutions[s].keySet().size();
            double wait_cost=waitTime;
            objections[s] = res+300*sum+wait_cost+pentytime;
        }
        if(count==0){
            System.out.println("again");
            creatFirstSolution1(start);
            calObjection2();
        }
    }

    private double[] calTaskTimes(List<Integer> list,int s){
        long ans=0;
        double pentyTime=0.0;
        Calendar calendar=new GregorianCalendar(2018,3,9,8,0);
        int size=list.size();
        int first=list.get(0);
        calendar.add(Calendar.MINUTE,data.times[0][first]);
        Date date = calendar.getTime();
        Date late=data.shops.get(first).last_receive_tm;
        Date early=data.shops.get(first).first_receive_tm;
        if(late.after(date)){
            if(date.before(early)){
                times[s].put(first,early);
            }else{
                times[s].put(first,date);
            }
            calendar.setTime(times[s].get(first));
            calendar.add(Calendar.MINUTE,30);//添加服务时间30分钟
        }else{
            System.out.println("160---objections[s]=Double.MAX_VALUE");
            System.exit(0);
        }
        for(int j=1;j<size;j++){
            if(list.get(j)>1000) continue;
            calendar.add(Calendar.MINUTE,data.times[list.get(j-1)][list.get(j)]);
            date=calendar.getTime();
            late=data.shops.get(list.get(j)).last_receive_tm;
            early=data.shops.get(list.get(j)).first_receive_tm;
            if(late.after(date)){
                if(date.before(early)){
                    times[s].put(list.get(j),early);
                    ans+=early.getTime()-date.getTime();
                }else{
                    times[s].put(list.get(j),date);
                }
                calendar.setTime(times[s].get(list.get(j)));
                calendar.add(Calendar.MINUTE,30);
            }else{
                pentyTime=10000+date.getTime()-late.getTime();//除以1000是秒
                times[s].put(list.get(j),date);
                calendar.setTime(date);
                calendar.add(Calendar.MINUTE,30);
            }
        }
        return new double[]{(ans*24)/(1000.0*60*60),pentyTime};
    }

    private double[] calDistance(List<Integer> list,int s,int i){
        double[] ans=new double[2];
        double cost=data.vehicles.get(vech_type).unit_trans_cost/1000.0;
        int size=list.size();
        int first=list.get(0);
        double distance=0.0;
        double res=0.0;
        res+=data.distances[0][first]*cost;
        distance+=data.distances[0][first];
        if(distance>data.vehicles.get(vech_type).driving_range){
            System.out.println("第一个任务不满足distance");
        }
        for(int j=1;j<size;j++){
            res+=data.distances[list.get(j-1)][list.get(j)]*cost;
            distance+=data.distances[list.get(j-1)][list.get(j)];
            if(distance>data.vehicles.get(vech_type).driving_range){
                System.out.println("179-----中间任务不满足distance");
                System.exit(0);
            }
        }
        int last=list.get(size-1);
        if(distance+data.distances[0][last]>data.vehicles.get(vech_type).driving_range){
            int nearestR=calNearestRecharge(last);
            distance+=data.distances[last][nearestR]+data.distances[nearestR][0];
            res+=data.distances[last][nearestR]*cost+data.distances[nearestR][0]*cost;
            res+=50;
            list.add(nearestR);
            solutions[s].put(i,list);
        }else{
            distance+=data.distances[0][last];
            res+=data.distances[0][last]*cost;
        }
        ans[0]=distance;
        ans[1]=res;
        return ans;
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

    public int calNearestRecharge(int customer){
        int nearestR=1001;double min=data.distances[customer][1001];
        for(int r=1002;r<=1100;r++){
            if(data.distances[customer][r]<min){
                min=data.distances[customer][r];
                nearestR=r;
            }
        }
        return nearestR;
    }

    public void update(){
        long s=System.currentTimeMillis();
        for(start=0;start<1000;start+=batch) {
            if(start%10==0) System.out.println(start);
            creatFirstSolution1(start);
            calObjection2();
            solutionFeasion();
        }
        System.out.println("总成本："+sums);
        long e=System.currentTimeMillis();
        testSolution();
        writeResultToCSVFile();
        System.out.println("求解时间："+(e-s)/1000.0);
    }

    public void solutionFeasion() {
        double min = objections[0];
        label = 0;
        for (int i = 1; i < size; i++) {
            if (objections[i] < min) {
                min = objections[i];
                label = i;
            }
        }
        //检测解是否可行
        HashMap<Integer, List<Integer>> solution = solutions[label];
        for (Integer key : solution.keySet()) {
            List<Integer> list = solution.get(key);
            double weight = 0.0;
            for (Integer task : list) {
                if (task > 1000) continue;
                weight += data.shops.get(task).pack_total_weight;
            }
            if (weight > data.vehicles.get(vech_type).max_weight) {
                System.out.println("weight");
                System.exit(0);
            }
            double volume = 0.0;
            for (Integer task : list) {
                if (task > 1000) continue;
                volume += data.shops.get(task).Pack_total_volume;
            }
            if (volume > data.vehicles.get(vech_type).max_volume) {
                System.out.println("volume");
                System.exit(0);
            }
            for (Integer task : list) {
                if (task > 1000) continue;
                if (times[label].get(task).after(data.shops.get(task).last_receive_tm)) {
                    System.out.println("time");
                    System.exit(0);
                }
            }
            int first = list.get(0);
            double distance = 0.0;
            distance += data.distances[0][first];
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) > 1000) {
                    distance += data.distances[list.get(i - 1)][list.get(i)];
                    if (distance > data.vehicles.get(vech_type).driving_range) {
                        System.out.println("driving range1," + distance);
                    } else {
                        distance = 0.0;
                    }
                } else {
                    distance += data.distances[list.get(i - 1)][list.get(i)];
                    if (distance > data.vehicles.get(vech_type).driving_range) {
                        System.out.println("driving range2," + distance);
                        System.exit(0);
                    }
                }
            }
            if (distance > data.vehicles.get(vech_type).driving_range) {
                System.out.println("driving range4," + distance);
            }
            distance += data.distances[0][list.get(list.size() - 1)];
            if (distance > data.vehicles.get(vech_type).driving_range) {
                System.out.println("driving range3," + distance);
                for (List<Integer> lists : solutions[label].values()) {
                    for (int i = 0; i < lists.size(); i++) {
                        System.out.println(lists.get(i) + " ");
                    }
                    System.exit(0);
                }
            }
        }
        //保存解的等待时间
        for (Integer k : waitTimes[label].keySet()) {
            double temp = waitTimes[label].get(k);
            allVechileWaitTime.put(vechSum1++, temp);
        }
        //将本次求的解和出发时间保存
        for (Integer k : solution.keySet()) {
            List<Integer> paths = solution.get(k);
            List<Integer> path = new ArrayList<Integer>();
            for (Integer p : paths) {
                path.add(p);
                if (p <= 1000) {
                    resultTimes.put(p, times[label].get(p));
                }
            }
            results.put(vechSum++, path);
        }
        sums += Double.parseDouble(String.format("%.2f",min));
        /*
        for(List<Integer> list:solution.values()){
            for(int i=0;i<list.size();i++){
                if(list.get(i)>1000) {
                    System.out.print(list.get(i)+" ");
                    continue;
                }
                if(times[label].get(list.get(i)).after(data.shops.get(list.get(i)).last_receive_tm)){
                    System.out.print(list.get(i)+"TW ");
                }else{
                    System.out.print(list.get(i)+" ");
                }
            }
            System.out.println();
        }*/
        }

    private void testSolution(){
        for(int i=1;i<=1000;i++){
            if(!resultTimes.keySet().contains(i)){
                System.out.println("task lacks");
            }
        }
        List<Integer> list=new ArrayList<Integer>();
        for(List<Integer> key:results.values()){
             for(Integer i:key){
                 if(i<=1000) list.add(i);
             }
        }
        if(list.size()!=1000){
            System.out.println("task<1000");
        }
    }

    private void writeResultToCSVFile(){
        try {
            String csvFilePath = "/Users/apple/Desktop/result/"+sums+".csv" ;
            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(csvFilePath, ',', Charset.forName("UTF-8"));
            SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm");
            double cost=data.vehicles.get(2).unit_trans_cost/1000.0;
            Calendar calendar=new GregorianCalendar(2017,10,23,2,58,0);
            String[] table={"trans_code","vehicle_type","dist_seq","distribute_lea_tm","distribute_arr_tm","distance","trans_cost","charge_cost","wait_cost","fixed_use_cost","total_cost","charge_cnt"};
            //String[] table={"派车单号","车型","顺序","配送中心出发时间","配送中心返回时间","里程","运输成本","充电成本","等待成本","固定成本","总成本","充电次数"};
            csvWriter.writeRecord(table);
            for(Integer vechile:results.keySet()) {
                String[] csvContent = new String[12];
                if(vechile<10) {
                    csvContent[0] = "DP000" +String.valueOf(vechile);
                }else if(vechile<100){
                    csvContent[0] = "DP00" +String.valueOf(vechile);
                }else if(vechile<1000){
                    csvContent[0] = "DP0" +String.valueOf(vechile);
                }
                csvContent[1]=String.valueOf(vech_type);
                List<Integer> path=results.get(vechile);
                csvContent[2]="0;";
                for(Integer p:path){
                    csvContent[2]+=String.valueOf(p)+";";
                }
                csvContent[2]+="0";
                List res=test(path);
                Date date1=(Date)res.get(0);
                String str = df.format(date1);
                csvContent[3]=str.split(" ")[1];
                Date date2=(Date)res.get(1);
                str=df.format(date2);
                csvContent[4]=str.split(" ")[1];
                csvContent[5]=String.valueOf(res.get(2));
                double distance=((Double) res.get(2))*cost;
                csvContent[6]=String.format("%.2f",distance);
                double charge=(Integer)res.get(3)*50;
                csvContent[7]=String.valueOf(charge);
                //double wait_cost=24*(Long)res.get(4)/(1000.0*60*60);
                //double wait_cost=24*waitTimes[label].get(vechile)/(1000.0*60*60);
                //double wait_cost=allVechileWaitTime.get(vechile);
                double wait_cost=calWaitTimes(path);
                double[] wait_cost1=calTaskTimes(path,1);
                if(wait_cost!=wait_cost1[0]){
                    System.out.println(wait_cost+"  "+wait_cost1[0]);
                    System.exit(0);
                }
                csvContent[8]=String.format("%.2f",wait_cost);
                csvContent[9]=String.valueOf(300);
                csvContent[10]=String.format("%.2f",distance+wait_cost+charge+300);
                csvContent[11]=String.valueOf(res.get(3));
                csvWriter.writeRecord(csvContent);
            }
            csvWriter.close();
            System.out.println("-------csv文件已经写入---------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List test(List<Integer> list){
        List res=new ArrayList();
        int size=list.size();
        if(size==0) return res;
        long waitTime=0;
        double distance=0.0;
        int recharge=0;
        int first=list.get(0);
        Date firstTime=resultTimes.get(first);
        Calendar calendar=new GregorianCalendar(2018,6,7,11,0);
        calendar.setTime(firstTime);
        calendar.add(Calendar.MINUTE,-data.times[0][first]);
        res.add(calendar.getTime());//加入配送中心出发时间
        calendar.setTime(firstTime);
        calendar.add(Calendar.MINUTE,30);
        distance+=data.distances[0][first];
        for(int i=1;i<size;i++){
            distance+=data.distances[list.get(i-1)][list.get(i)];
            if(list.get(i)<=1000){
                firstTime=resultTimes.get(list.get(i));
                Date early=data.shops.get(list.get(i)).first_receive_tm;
                Date late=data.shops.get(list.get(i)).last_receive_tm;
                if(firstTime.after(late)){
                    System.out.println("error firstTime.after(late)");
                    System.exit(0);
                }
                if(firstTime.before(early)){
                    calendar.setTime(early);
                }else{
                    calendar.setTime(firstTime);
                }
            }else{
                calendar.add(Calendar.MINUTE,data.times[list.get(i-1)][list.get(i)]);
            }
            calendar.add(Calendar.MINUTE,30);
        }
        int last=list.get(size-1);
        if(last>1000){
            recharge++;
        }
        distance+=data.distances[0][last];
        calendar.add(Calendar.MINUTE,data.times[0][last]);
        res.add(calendar.getTime());
        res.add(distance);
        res.add(recharge);
        res.add(waitTime);
        return res;
    }

    private double calWaitTimes(List<Integer> list){
        double ans;
        long waits=0;
        int first=list.get(0);
        Calendar calendar=new GregorianCalendar(2018,3,9,8,0);
        calendar.add(Calendar.MINUTE,data.times[0][first]);//第一个任务开始执行时间
        Date firstTime=calendar.getTime();
        if(firstTime.before(data.shops.get(first).first_receive_tm)){
            calendar.setTime(data.shops.get(first).first_receive_tm);
        }else{
            calendar.setTime(firstTime);
        }
        calendar.add(Calendar.MINUTE,30);//第一个任务执行完成时间
        for(int i=1;i<list.size();i++){
            if(list.get(i)<=1000){
                calendar.add(Calendar.MINUTE,data.times[list.get(i-1)][list.get(i)]);
                firstTime=calendar.getTime();
                Date early=data.shops.get(list.get(i)).first_receive_tm;
                if(firstTime.before(early)){
                    calendar.setTime(early);
                    waits+=early.getTime()-firstTime.getTime();
                }
                calendar.add(Calendar.MINUTE,30);
            }
        }
        ans=24*waits/(1000.0*60*60);
        return ans;
    }
}
