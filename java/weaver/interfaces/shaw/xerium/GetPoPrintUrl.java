package weaver.interfaces.shaw.xerium;

import weaver.general.BaseBean;

/**
 * Created by adore on 2017/4/26.
 * ��ҳ�б��������
 */
public class GetPoPrintUrl extends BaseBean {

    public String getPrintUrl(String var1, String var2) {
        String var6 = "<a href='/xerium/x_purchaseOrderPrint.jsp?requestId=" + var1 + "&supid=" + var2 + " ' target='_blank'>��ӡ</a>";
        return var6;
    }
}
