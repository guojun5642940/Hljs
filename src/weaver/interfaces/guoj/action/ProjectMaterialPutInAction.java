package weaver.interfaces.guoj.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestComInfo;
import weaver.workflow.workflow.WorkflowComInfo;
import weaver.interfaces.guoj.util.ActionMapUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProjectMaterialPutInAction  implements Action {

    private RequestComInfo requestComInfo;
    private weaver.workflow.workflow.WorkflowComInfo WorkflowComInfo;
    private BaseBean bs;

    public ProjectMaterialPutInAction() {
        try {
            requestComInfo = new RequestComInfo();
            WorkflowComInfo = new WorkflowComInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        bs = new BaseBean();
    }

    @java.lang.Override
    public String execute(RequestInfo request) {
        String workflowId = request.getWorkflowid();
        String requestId = request.getRequestid();
        String requestName = requestComInfo.getRequestname(requestId);
        String workflowName = WorkflowComInfo.getWorkflowname(workflowId);
        bs.writeLog("ActionInterceptorService--进入action--workflowId：{"+workflowId+"},workflowName：{"+workflowName+"},requestId：{"+requestId+"},requestName：{"+requestName+"}");

        ActionMapUtil aUtil = new ActionMapUtil(request);
        Map dataMap = aUtil.getDataMap();
        String xmbh = Util.null2String(dataMap.get("xmbh".toLowerCase()).toString());
        String xmmc = Util.null2String(dataMap.get("xmmc".toLowerCase()).toString());
        String rkdbh = Util.null2String(dataMap.get("rkdbh".toLowerCase()).toString());
        String rq = Util.null2String(dataMap.get("rq".toLowerCase()).toString());

        //查询到建模表中对应的项目数据
        String sql = "select id,zrksl,sjkcsl,rkzj from uf_yskm where xmbh = '"+xmbh+"'";
        RecordSet rs = new RecordSet();
        RecordSet rsUpdate = new RecordSet();
        rs.execute(sql);
        if(rs.next()){
            String id = Util.null2String(rs.getString("id"));
            int zrksl = Util.getIntValue(rs.getString("zrksl"));//总入库数量
            int sjkcsl = Util.getIntValue(rs.getString("sjkcsl"));//实际库存数量
            double rkzj = Util.getDoubleValue(rs.getString("rkzj"));//入库总价

            bs.writeLog("requestid=["+requestId+"],xmmc=["+xmmc+"]查询建模数据ID["+id+"]");
            ArrayList<HashMap> mx1List = new ArrayList<>();
            for(Map map:mx1List){
                String clkm = map.get("clkm").toString();
                String sssl = map.get("sssl").toString();
                String dj = map.get("dj").toString();
                String je = map.get("je").toString();
                String bz = map.get("bz").toString();

                sql = "insert into uf_yskm_dt1 (mainid,rkdh,rkrq,clkm,rksl,rkdj,rkje,bz,lcid)" +
                        " values ('"+id+"','"+rkdbh+"','"+rq+"','"+clkm+"','"+sssl+"','"+dj+"','"+je+"','"+bz+"','"+requestId+"')";
                bs.writeLog("requestid=["+requestId+"],xmmc=["+xmmc+"]插入建模数据sql["+sql+"]");
                rsUpdate.execute(sql);
                zrksl += Integer.parseInt(sssl);
                sjkcsl += Integer.parseInt(sssl);
                rkzj += Double.parseDouble(je);
            }
            //更新项目建模数据主表
            sql = "update uf_yskm set zrksl = '"+zrksl+"',sjkcsl = '"+sjkcsl+"',rkzj = '"+rkzj+"' where id = '"+id+"'";
            bs.writeLog("requestid=["+requestId+"],xmmc=["+xmmc+"]更新建模主表数据sql["+sql+"]");
        }else{
            bs.writeLog("requestid=["+requestId+"],xmmc=["+xmmc+"]未查到对应的建模数据");
        }
        return SUCCESS;
    }
}
