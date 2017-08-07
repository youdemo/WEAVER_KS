package weaver.interfaces.shaw.ddbz.tqsp;

import com.alibaba.fastjson.JSONObject;
import corp.openapicalls.contract.Authentification;
import corp.openapicalls.contract.setapproval.request.CurrencyType;
import corp.openapicalls.contract.setapproval.request.FlightEndorsementDetail;
import corp.openapicalls.contract.setapproval.request.HotelEndorsementDetail;
import corp.openapicalls.contract.setapproval.request.SetApprovalRequest;
import corp.openapicalls.contract.setapproval.request.SetApprovalServiceRequest;
import corp.openapicalls.contract.setapproval.response.SetApprovalResponse;
import corp.openapicalls.contract.ticket.TicketResponse;
import corp.openapicalls.service.setapproval.SetApprovalService;
import corp.openapicalls.service.ticket.CorpTicketService;
import weaver.interfaces.shaw.ddbz.webservice.PlatformStub;
import weaver.interfaces.shaw.ddbz.webservice.ApprovalStub;

import java.util.ArrayList;
import java.util.Date;
import java.lang.String;


/**
 * Created by adore on 2017/6/2.
 * 大道包装，提前审批
 */
public class ApproveAhead {
    private static void setApproval() {
        TicketResponse ticketResponse = CorpTicketService.getOrderAuditTicket("obktest", "****", "1.0");
        Date date = new Date();
        if (ticketResponse != null && ticketResponse.getStatus() != null && ticketResponse.getStatus().getSuccess()) {
            SetApprovalService setapprovalService = new SetApprovalService();
            SetApprovalServiceRequest setApprovalServiceRequest = new SetApprovalServiceRequest();
            SetApprovalRequest setApprovalRequest = new SetApprovalRequest();
            Authentification authInfo = new Authentification("", ticketResponse.getTicket());
            setApprovalRequest.setAuth(authInfo);
            setApprovalRequest.setApprovalNumber("776");
            setApprovalRequest.setCtripCardNO("lxm002");
            setApprovalRequest.setExpiredTime("2016-10-10");
            setApprovalRequest.setStatus(1);
            ArrayList<FlightEndorsementDetail> flightEndorsementDetails = new ArrayList();
            FlightEndorsementDetail flightEndorsementDetail = new FlightEndorsementDetail();
            flightEndorsementDetail.setAirline("MU");
            flightEndorsementDetail.setArrivalBeginTime("09:00");
            flightEndorsementDetail.setArrivalEndTime("19:00");
            flightEndorsementDetail.setCurrency(CurrencyType.RMB);
            flightEndorsementDetails.add(flightEndorsementDetail);

            ArrayList<HotelEndorsementDetail> hotelEndorsementDetails = new ArrayList();
            HotelEndorsementDetail hotelEndorsementDetail = new HotelEndorsementDetail();
            hotelEndorsementDetail.setAveragePrice(150);
            hotelEndorsementDetail.setCheckInBeginDate(new Date());
            hotelEndorsementDetail.setCurrency(CurrencyType.RMB);
            ArrayList<String> citys = new ArrayList();
            citys.add("beijing");
            hotelEndorsementDetail.setToCities(citys);
            hotelEndorsementDetails.add(hotelEndorsementDetail);
            setApprovalRequest.setHotelEndorsementDetails(hotelEndorsementDetails);
            setApprovalServiceRequest.setRequest(setApprovalRequest);
            SetApprovalResponse setApprovalResponse = setapprovalService.SetApproval(setApprovalServiceRequest);
            if (setApprovalResponse != null && setApprovalResponse.getStatus() != null) {
                System.out.printf("service result:%s", JSONObject.toJSONString(setApprovalResponse.getStatus()));
            }
        }
    }

    public static void main(String[] args){
        ApproveAhead aa = new ApproveAhead();
        aa.setApproval();
    }

}
