// E7浮点数计算与赋值(金额千分位)
jQuery(document).ready(function () {
    var totalAmount = "#field6367";//原币种
    var totalUSD = "#field6378";//目标币种
    var exchangeRate = "#field6424"; //汇率
    jQuery(totalAmount).bind("propertychange", function () {
        var totalAmount_val = jQuery(totalAmount).val();
        if (totalAmount_val.length > 0) {
            totalAmount_val = rmoney(totalAmount_val);
        }
        var exchangeRate_val = jQuery(exchangeRate).val();
        var sum = accMul(totalAmount_val, exchangeRate_val);
        //alert("sum1=" + sum);
        if (sum > 0) {
            sum = fmoney(sum, 2);
        }
        //alert("sum2=" + sum);
        jQuery(totalUSD).val(sum);
        jQuery(totalUSD + "span").html(sum);
    });

    jQuery(exchangeRate).bind("propertychange", function () {
        var totalAmount_val = jQuery(totalAmount).val();
        if (totalAmount_val.length > 0) {
            totalAmount_val = rmoney(totalAmount_val);
        }
        var exchangeRate_val = jQuery(exchangeRate).val();
        var sum = accMul(totalAmount_val, exchangeRate_val);
        //alert("sum1=" + sum);
        if (sum > 0) {
            sum = fmoney(sum, 2);
        }
        //alert("sum2=" + sum);
        jQuery(totalUSD).val(sum);
        jQuery(totalUSD + "span").html(sum);
    });
});

//浮点数乘法计算
function accMul(arg1, arg2) {
    var m = 0,
        s1 = arg1.toString(),
        s2 = arg2.toString();
    try {
        m += s1.split(".")[1].length
    } catch (e) {}
    try {
        m += s2.split(".")[1].length
    } catch (e) {}
    //alert("m=" + m);
    return Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m)
}

//浮点数转换为金额千分位
function fmoney(s, n) {
    n = n > 0 && n <= 20 ? n : 2;
    s = parseFloat((s + "").replace(/[^\d\.-]/g, "")).toFixed(n) + "";
    var l = s.split(".")[0].split("").reverse(),
        r = s.split(".")[1];
    t = "";
    for (i = 0; i < l.length; i++) {
        t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : "");
    }
    return t.split("").reverse().join("") + "." + r;
}

//金额千分位转换为浮点数
function rmoney(s) {
    s = s.toString();
    return parseFloat(s.replace(/[^\d\.-]/g, ""));
}