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
 * Я�̵����¼���Խӿ�
 */
public class TestLoginCtrip {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Ticket��ȡURL
        String ticketUrl = "https://ct.ctrip.com/corpservice/authorize/getticket";
        String appKey = "***";//Я���ṩ
        String appSecurity = "***";//Я���ṩ
        //Json��
        String ticketPostString = "{\"appKey\":\"" + appKey + "\",\"appSecurity\":\"" + appSecurity + "\"}";
        //POST
        String ticketResponse = sendPost(ticketUrl, ticketPostString);
        //����JSON����,��Ҫ����net.sf.json��
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(ticketResponse);
        //��ȡTicket
        String ticket = (String) jsonObject.get("Token");

        //�����¼URL
        String loginUrl = null;
        String responseText = null;
        String employeeID = "***";
        String signature = "***";
        String TA = "N000001";
        String cost1 = "�ɱ�����1";
        String cost2 = "�ɱ�����2";
        String cost3 = "�ɱ�����3";

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
        //���ñ���ΪUTF-8
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
            conn = (HttpURLConnection) realUrl.openConnection();// �򿪺�URL֮�������

            // ����POST�������������������
            conn.setRequestMethod("POST"); // POST����
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // ����ͨ�õ���������
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.connect();

            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");// ��ȡURLConnection�����Ӧ�������
            out.write(param);// �����������
            out.flush();// flush������Ļ���

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));//����BufferedReader����������ȡURL����Ӧ

            String line;
            while ((line = in.readLine()) != null) {
                result += line;
                System.out.println("OK");
            }
        } catch (Exception e) {
            System.out.println("���� POST ��������쳣��" + e);
            e.printStackTrace();
        }
        //ʹ��finally�����ر��������������
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
