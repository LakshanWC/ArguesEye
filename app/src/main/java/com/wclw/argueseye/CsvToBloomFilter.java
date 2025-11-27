package com.wclw.argueseye;

import android.content.Context;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CsvToBloomFilter {


    public boolean buildBloomFilter(Context context,String filePath,int expectedInsertions,double falsePositives){

        BloomFilter<CharSequence> bloomFilter = BloomFilter.create(
                Funnels.stringFunnel(StandardCharsets.UTF_8),
                expectedInsertions,
                falsePositives
        );

        String fileName = filePath.substring(filePath.lastIndexOf(('/')+1));

        try(InputStream inputStream = context.getAssets().open(filePath);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream)))
        {
            String line;
            while((line= bufferedReader.readLine())!=null){
                line = line.trim();
                if(!line.isEmpty()){
                    bloomFilter.put(line);
                }
            }
            bufferedReader.close();
            inputStream.close();

            return saveBloomFilter(context,bloomFilter,fileName);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean saveBloomFilter(Context context,BloomFilter<CharSequence> bloomFilter,String filterName){
        try{

            FileOutputStream fileOutputStream = context.openFileOutput(filterName+".bloom",Context.MODE_PRIVATE);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);

            outputStream.writeObject(bloomFilter);
            fileOutputStream.close();
            outputStream.close();
            return true;
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }

}
