package quant._ver1.web.controller;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import quant._ver1.web.data.Sangjang;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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
        List<CompanyCompareClass> company_per_code = new ArrayList();
        List<CompanyCompareClass> company_pbr_code = new ArrayList();
        HashMap<String, Integer> company_point_hash = new HashMap();

        for (int i = 0; i < temp_max; i++) {
            String companySixCode = companyCodeCnvToSixNumber(company_code[i]);

            String URL = "https://finance.naver.com/item/sise.naver?code=" + companySixCode;
            Document doc = Jsoup.connect(URL).get();
            Elements select = doc.select("#_sise_market_sum");

            company_market_code.add(new Company(companyMarketCapCnvToInteger(select.text()), companySixCode, company_name[i]));
        }

        Collections.sort(company_market_code);

        int max_company_cnt = 0;
        for (Company company : company_market_code) {
            company_point_hash.put(company.code, 0);
            System.out.println("[marketCap]:"+ company.marketCap + "  company.code: " + company.code + " company.name " + company.name);
            //String URL_2 = "https://finance.naver.com/item/sise.naver?code=" + company.code;

            String URL_2 = "https://finance.naver.com/item/main.naver?code=" + company.code;
            Document doc_2 = Jsoup.connect(URL_2).get();

            //PER
            //Elements select_per = doc_2.select("#_sise_per");
            Elements select_per = doc_2.select("#content > div.section.trade_compare > table > tbody > tr:nth-child(13) > td:nth-child(2)");
            System.out.println("select_per = " + select_per + " company name: " + company.name);
            company_per_code.add(new CompanyCompareClass(companyFigureCnvToFloat(select_per.text()), company.code, company.name));

            //PBR
            //#content > div.section.trade_compare > table > tbody > tr:nth-child(14) > td:nth-child(2)
            Elements select_pbr = doc_2.select("#content > div.section.trade_compare > table > tbody > tr:nth-child(14) > td:nth-child(2)");
            System.out.println("select_pbr = " + select_pbr + "company name: " + company.name);
            company_pbr_code.add(new CompanyCompareClass(companyFigureCnvToFloat(select_pbr.text()), company.code, company.name));

            //차라리 백데이터? 아무튼 그 어떤날에 얼마인지 확인하는거 해야함
            max_company_cnt = max_company_cnt + 1;
            if (max_company_cnt == 100) {
                break;
            }
        }
        Collections.sort(company_per_code);
        Collections.sort(company_pbr_code);

        int temp1 = 1;
        for (CompanyCompareClass companyCompareClass : company_pbr_code) {
            int temp = company_point_hash.get(companyCompareClass.code);
            if (companyCompareClass.compareFloat == 99999) {
                company_point_hash.put(companyCompareClass.code, temp + 500);
            } else {
                company_point_hash.put(companyCompareClass.code, temp1+temp);
            }
            System.out.println("[pbr]:"+ companyCompareClass.compareFloat + "  company.code: " + companyCompareClass.code + " company.name " + companyCompareClass.name);
            temp1 = temp1 + 1;
        }
        int temp2 = 1;
        for (CompanyCompareClass companyCompareClass : company_per_code) {
            int temp = company_point_hash.get(companyCompareClass.code);
            if (companyCompareClass.compareFloat == 99999) {
                company_point_hash.put(companyCompareClass.code, temp + 500);
            } else {
                company_point_hash.put(companyCompareClass.code, temp2+temp);
            }
            System.out.println("[per]:"+ companyCompareClass.compareFloat + "  company.code: " + companyCompareClass.code + " company.name " + companyCompareClass.name);

            temp2 = temp2 + 1;
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

    public Float companyFigureCnvToFloat(String company_text) {
        int temp = 99999;
        if (company_text.isEmpty()) {
            return Float.valueOf(temp);
        }
        company_text = company_text.replaceAll(",", "");
        if (company_text.compareTo("N/A") == 0) {
            return Float.valueOf(temp);
        } else {
            if (company_text.charAt(0) == '-') {
                //return -Float.valueOf(company_text.substring(1));
                return Float.valueOf(temp);
            } else if (company_text.charAt(0) == '∞') {
                return Float.valueOf(temp);
            } else if (company_text.charAt(0) == '.') {
                company_text = '0' + company_text;
                return Float.valueOf(company_text);
            } else {
                return Float.valueOf(company_text);
            }
        }
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
    static class CompanyCompareClass implements Comparable<CompanyCompareClass>{
        private float compareFloat;
        private String code;
        private String name;

        public CompanyCompareClass(float compareFloat, String code, String name) {
            this.compareFloat = compareFloat;
            this.code = code;
            this.name = name;
        }

        @Override
        public int compareTo(CompanyCompareClass c) {
            if (this.compareFloat > c.compareFloat) {
                return 1;
            } else if (this.compareFloat < c.compareFloat) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
