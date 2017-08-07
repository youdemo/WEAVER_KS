package weaver.interfaces.shaw.ddbz;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by adore on 2017/5/22.
 * 携程单点登录测试接口
 */
public class TestLoginCtrip {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ticket获取URL
        String ticketUrl = "https://ct.ctrip.com/corpservice/authorize/getticket";
        String appKey = "***";//携程提供
        String appSecurity = "***";//携程提供
        //Json串
        String ticketPostString = "{\"appKey\":\"" + appKey + "\",\"appSecurity\":\"" + appSecurity + "\"}";
        //POST
        String ticketResponse = sendPost(ticketUrl, ticketPostString);
        //构造JSON对象,需要导入net.sf.json包
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(ticketResponse);
        //获取Ticket
        String ticket = (String) jsonObject.get("Token");

        //单点登录URL
        String loginUrl = null;
        String responseText = null;
        String employeeID = "***";
        String signature = "***";
        String TA = "N000001";
        String cost1 = "成本中心1";
        String cost2 = "成本中心2";
        String cost3 = "成本中心3";

        loginUrl = "https://ct.ctrip.com/corpservice/authorize/login";
        responseText = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">";
        responseText = responseText + "<script type=\"text/javascript\">function formSubmit(){document.getElementById(\"fLogin\").submit();}</script>";
        responseText = responseText + "</head><body>";
        responseText = responseText + "<form name=\"fLogin\" id=\"fLogin\" method=\"post\" action=\"" + loginUrl + "\">";
        responseText = responseText + "<input type=\"hidden\" name=\"AppKey\" value=\"" + appKey + "\" />";
        responseText = responseText + "<input type=\"hidden\" name=\"Ticket\" value=\"" + ticket + "\" />";
        responseText = responseText + "<input type=\"hidden\" name=\"EmployeeID\" value=\"" + employeeID + "\"/>";
        responseText = responseText + "<input type=\"hidden\" name=\"Signature \" value=\"" + signature + "\"/>";
        responseText = responseText + "<input type=\"hidden\" name=\"TA\" value=\"" + TA + "\"/>";
        responseText = responseText + "<input type=\"hidden\" name=\"Cost1\" value=\"" + cost1 + "\"/>";
        responseText = responseText + "<input type=\"hidden\" name=\"Cost2\" value=\"" + cost2 + "\"/>";
        responseText = responseText + "<input type=\"hidden\" name=\"Cost3\" value=\"" + cost3 + "\"/>";
        responseText = responseText + "<script language=\"javascript\">formSubmit();</script></form></body>";
        //设置编码为UTF-8
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        PrintWriter pw = response.getWriter();
        pw.println(responseText);
        System.out.println("getRequestURL: "+request.getRequestURL());
    }

    //Post method
    public static String sendPost(String url, String param) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = null;
            conn = (HttpURLConnection) realUrl.openConnection();// 打开和URL之间的连接

            // 发送POST请求必须设置如下两行
            conn.setRequestMethod("POST"); // POST方法
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();

            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");// 获取URLConnection对象对应的输出流
            out.write(param);// 发送请求参数
            out.flush();// flush输出流的缓冲

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));//定义BufferedReader输入流来读取URL的响应

            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                System.out.println("OK");
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！" + e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }
}
