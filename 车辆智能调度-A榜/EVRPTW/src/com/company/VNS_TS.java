package com.company;

import java.util.*;

public class VNS_TS {
    final int size=10000;
    Data data;
    List<List<Integer>>[] solutions;
    double[] objections;

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
        int count = data.shops.keySet().size();
        for (int i = 1; i < size; i++) {
            final int num = 1 + (int) (Math.random() * (count));//随机选择一个点
            Queue<Integer> queue = new PriorityQueue<Integer>(new Comparator<Integer>() {
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
            List<Integer> list = new ArrayList<Integer>();
            list.add(num);
            while(!queue.isEmpty()) {
                int j=queue.poll();
                weights += data.shops.get(j).pack_total_weight;
                volumes += data.shops.get(j).getPack_total_volume;
                if (weights <= data.vehicles.get(1).max_weight && volumes <= data.vehicles.get(1).max_volume) {
                    list.add(j);
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
        for(int s=0;s<size;s++) {
            List<List<Integer>> solution = solutions[s];
            double res = 0.0;
            //考虑电量约束，判断时间窗是否满足

            objections[s] = res;
        }
    }
}
