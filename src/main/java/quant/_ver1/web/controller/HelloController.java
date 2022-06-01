package quant._ver1.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import quant._ver1.web.data.Sangjang;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.management.Query;
import java.io.IOException;
import java.util.*;

@Controller
public class HelloController {
    @GetMapping("hello")
    public String hello(Model model) throws IOException {
        model.addAttribute("data", "spring!!");

//        String URL = "https://finance.naver.com/item/coinfo.naver?code=005930&target=finsum_more";
//        Elements select = doc.select("#tab_con1 > div.first > table > tbody > tr:nth-child(2) > th");
//        for (Element element : select) {
//            System.out.println("element.text() = " + element.text());
//        }
        String URL = "https://finance.naver.com/item/sise.naver?code=005930";
        Document doc = Jsoup.connect(URL).get();
        Elements select = doc.select("#_sise_market_sum");
        for (Element element : select) {
            System.out.println("element.text() = " + element.text());

        }


        //#cTB11 > tbody > tr:nth-child(5) > td
        //Elements select = doc.select(".body");
        
        return "hello";
    }

    @GetMapping("sangjang")
    public String sangjangInit(Model model) throws IOException {
        Sangjang sj = new Sangjang();
        int[] company_code = sj.getCompany_code();
        String[] company_name = sj.getCompany_name();
        int temp_max = 100;
        List company_list = new ArrayList();
        List<Company> company_market_code = new ArrayList();
        for (int i = 0; i < temp_max; i++) {
            String companySixCode = companyCodeCnvToSixNumber(company_code[i]);

            String URL = "https://finance.naver.com/item/sise.naver?code=" + companySixCode;
            Document doc = Jsoup.connect(URL).get();
            Elements select = doc.select("#_sise_market_sum");
            company_market_code.add(new Company(companyMarketCapCnvToInteger(select.text()), companySixCode, company_name[i]));
        }
        Collections.sort(company_market_code);
        for (Company company : company_market_code) {

            String URL_2 = "https://finance.naver.com/item/sise.naver?code=" + company.code;
            Document doc_2 = Jsoup.connect(URL_2).get();
            Elements select_2 = doc_2.select("#_sise_per");
            //PER
            System.out.println("company.marketCap = " + company.marketCap + " code = " + company.code + " 회사 = " + company.name + " per = " + select_2.text());

            //차라리 백데이터? 아무튼 그 어떤날에 얼마인지 확인하는거 해야함
        }



        return "sangjang";
    }

    @GetMapping("hello-mvc")
    public String helloMvc(@RequestParam("name") String name, Model model) {
        model.addAttribute("name", name);
        return "hello-template";
    }

    @GetMapping("hello-string")
    @ResponseBody
    public String helloString(@RequestParam("name") String name) {
        return "hello " + name; //hello spring 등등...
    }

    @GetMapping("hello-api")
    @ResponseBody
    public Hello helloApi(@RequestParam("name") String name) {

        Hello hello = new Hello();
        hello.setName(name);
        return hello;
    }
    public String companyCodeCnvToSixNumber(int company_code) {
        String s = Integer.toString(company_code);
        int length = s.length();

        if (length != 6) {
            int temp = 6 - length;
            String input_zero = "";
            for (int i = 0; i < temp; i++) {
                input_zero += "0";
            }
            return input_zero + s;
        }
        return s;
    }
    public Integer companyMarketCapCnvToInteger(String company_text) {
        String s = company_text.replaceAll(",", "");
        return Integer.valueOf(s);
    }

    static class Hello {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    static class Company implements Comparable<Company>{
        private int marketCap;
        private String code;
        private String name;

        @Override
        public int compareTo(Company c) {
            if (this.marketCap > c.marketCap) {
                return 1;
            } else if (this.marketCap < c.marketCap) {
                return -1;
            } else {
                return 0;
            }
        }

        public int getMarketCap() {
            return marketCap;
        }

        public void setMarketCap(int marketCap) {
            this.marketCap = marketCap;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Company(int marketCap, String code, String name) {
            this.marketCap = marketCap;
            this.code = code;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return "Company{" +
                    "marketCap=" + marketCap +
                    ", code='" + code + '\'' +
                    '}';
        }
    }
}
