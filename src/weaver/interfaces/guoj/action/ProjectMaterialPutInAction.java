package weaver.interfaces.guoj.action;

import weaver.conn.RecordSet;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;
import weaver.workflow.request.RequestComInfo;
import weaver.workflow.workflow.WorkflowComInfo;
import weaver.interfaces.guoj.util.ActionMapUtil;

import java.text.DecimalFormat;
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

        String sql = "";
        String insertSql = "";
        String updateSql = "";
        RecordSet rs = new RecordSet();
        RecordSet rsInsert = new RecordSet();
        ArrayList<HashMap> mx1List = new ArrayList<>();
        mx1List = aUtil.getMXDataMap(request,1);
        for(Map map:mx1List){
            String yskm = map.get("yskm").toString();
            String clkm = map.get("clkm").toString();
            String sssl = map.get("sssl").toString();
            String dj = map.get("dj").toString();
            String je = map.get("je").toString();
            String bz = map.get("bz").toString();

            //查询到建模表中对应的项目数据
            sql = "select id,zcksl from uf_yskm where xmbh = '"+xmbh+"' and km = '"+yskm+"'";
            bs.writeLog("requestid=["+requestId+"],xmmc=["+xmmc+"]查询建模主表sql["+sql+"]");
            rs.execute(sql);

            if(rs.next()){
                String id = Util.null2String(rs.getString("id"));
                double zcksl = Util.getDoubleValue(rs.getString("zcksl"),0);//总出库数量
                insertSql = "insert into uf_yskm_dt1 (mainid,rkdh,rkrq,clkm,rksl,rkdj,rkje,bz,lcid)" +
                        " values ('"+id+"','"+rkdbh+"','"+rq+"','"+clkm+"','"+sssl+"','"+dj+"','"+je+"','"+bz+"','"+requestId+"')";
                bs.writeLog("requestid=["+requestId+"],xmmc=["+xmmc+"]插入建模数据sql["+insertSql+"]");
                rsInsert.execute(insertSql);
                updateModeinfo(id,zcksl,requestId);
            }else{
                bs.writeLog("requestid=["+requestId+"],xmmc=["+xmmc+"]未查到"+xmbh+"----"+yskm+"对应的建模数据");
            }
        }
        return SUCCESS;
    }

    /**
     * 更新建模主表数据
     */
    public void updateModeinfo(String id,double zcksl,String requestId){
        double zrksl = 0;//总入库数量
        double rkzj = 0;//入库总价
        String sql = "select * from uf_yskm_dt1 where mainid = '"+id+"'";
        RecordSet rs = new RecordSet();
        rs.execute(sql);
        while(rs.next()){
            double rksl = Util.getDoubleValue(rs.getString("rksl"));
            double rkje = Util.getDoubleValue(rs.getString("rkje"));
            zrksl += rksl;
            rkzj += rkje;
        }
        double kcpjdj = rkzj/zrksl;
        DecimalFormat df = new DecimalFormat("#.0000");
        sql = "update uf_yskm set zrksl = '"+zrksl+"',rkzj='"+rkzj+"',sjkcsl='"+(zrksl-zcksl)+"',kcpjdj='"+df.format(kcpjdj)+"' where id = '"+id+"'";
        bs.writeLog("requestid=["+requestId+"],updateModeinfo--"+sql);
        rs.execute(sql);
    }
}
