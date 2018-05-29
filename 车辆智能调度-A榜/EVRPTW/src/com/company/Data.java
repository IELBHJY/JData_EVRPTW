package com.company;

import com.csvreader.CsvReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class Data {
    int deport;
    double lng;
    double lat;
    Date first_tm;
    Date last_tm;
    public int[][] distances;
    public int[][] times;
    public HashMap<Integer,Node> shops;
    public HashMap<Integer,Recharge> recharges;
    public HashMap<Integer,Vehicle> vehicles;
    private static SimpleDateFormat hms = new SimpleDateFormat(  "HH:mm");
    public void readTransportionData()
    {
        shops=new HashMap<Integer, Node>();
        recharges=new HashMap<Integer, Recharge>();
        try {
            ArrayList<String[]> csvFileList = new ArrayList<String[]>();
            String csvFilePath = "/Users/apple/Documents/车辆智能调度-A榜/input_node.csv";
            CsvReader reader = new CsvReader(csvFilePath, ',', Charset.forName("UTF-8"));
            while (reader.readRecord()){
                csvFileList.add(reader.getValues());
            }
            reader.close();
            String[] strData = csvFileList.get(1);
            deport=Integer.parseInt(strData[0]);
            lng=Double.parseDouble(strData[2]);
            lat=Double.parseDouble(strData[3]);
            first_tm=hms.parse(strData[6]);
            last_tm=hms.parse(strData[7]);
            for (int i = 2; i<csvFileList.size(); i++) {
                strData = csvFileList.get(i);
                if(i<1002){
                    Node node=new Node();
                    node.id=Integer.parseInt(strData[0]);
                    node.lng=Double.parseDouble(strData[2]);
                    node.lat=Double.parseDouble(strData[3]);
                    node.pack_total_weight=Double.parseDouble(strData[4]);
                    node.getPack_total_volume=Double.parseDouble(strData[5]);
                    node.first_receive_tm=hms.parse(strData[6]);
                    node.last_receive_tm=hms.parse(strData[7]);
                    shops.put(node.id,node);
                }else{
                    Recharge recharge=new Recharge();
                    recharge.id=Integer.parseInt(strData[0]);
                    recharge.lng=Double.parseDouble(strData[2]);
                    recharge.lat=Double.parseDouble(strData[3]);
                    recharges.put(recharge.id,recharge);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readDistanceTime(){
        distances=new int[1101][1101];
        times=new int[1101][1101];
        String path="/Users/apple/Documents/车辆智能调度-A榜/input_distance-time.txt";
        try {
            Scanner cin = new Scanner(new BufferedReader(new FileReader(path)));
            String line=cin.nextLine();
            while(cin.hasNext()){
                line=cin.nextLine();
                String[] lines=line.trim().split(",");
                distances[Integer.parseInt(lines[1])][Integer.parseInt(lines[2])]=(
                        Integer.parseInt(lines[3]));
                times[Integer.parseInt(lines[1])][Integer.parseInt(lines[2])]=(
                        Integer.parseInt(lines[4]));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void readVehicle(){
        vehicles=new HashMap<Integer, Vehicle>();
        Vehicle vehicle=new Vehicle();
        vehicle.id=1;
        vehicle.max_volume=12;
        vehicle.max_weight=2;
        vehicle.driving_range=100000;
        vehicle.charge_tm=0.5;
        vehicle.unit_trans_cost=12;
        vehicle.vehicle_cost=200;
        vehicles.put(1,vehicle);
        Vehicle vehicle1=new Vehicle();
        vehicle1.id=2;
        vehicle1.max_volume=16;
        vehicle1.max_weight=2.5;
        vehicle1.driving_range=120000;
        vehicle1.charge_tm=0.5;
        vehicle1.unit_trans_cost=14;
        vehicle1.vehicle_cost=300;
        vehicles.put(2,vehicle1);
    }
}
