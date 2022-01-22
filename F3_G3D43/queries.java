import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class queries {
    private static final String URL = "jdbc:postgresql://10.62.73.22:5432/";
    private static final String username = "l3d43";
    private static final String password = "l3d43";
    private static Connection con = null;
    private static Statement stmt = null;
    private static PreparedStatement pstmt = null;
    private static ResultSet rs = null;
    private static final int SIZE_NUMBER_PART_ID = 4;

    public static List<Integer> getIdTipos() throws SQLException {return stQueryResListInt("select id from ACTIVOTIPO");}
    public static List<Integer> getEquipas() throws SQLException {return stQueryResListInt("select distinct equipa from pessoa");}
    public static List<String> getAtivos() throws SQLException {return stQueryResListString("select nome from ACTIVO");}
    public static List<String> getIdAtivos() throws SQLException {return stQueryResListString("select id from ACTIVO");}
    public static List<String> getNomePessoas() throws SQLException {return stQueryResListString("select nome from PESSOA");}
    public static List<String> getEmpresas() throws SQLException {return stQueryResListString("select nome from EMPRESA");}
    public static String getIdAtivo(String name) throws SQLException{return pstQueryResString("select id from ACTIVO where nome = ?", name);}
    public static int getIdPessoa(String name) throws SQLException{return pstQueryResInt("select id from PESSOA where nome = ?", name);}
    public static int getIdEmpresa(String name) throws SQLException{return pstQueryResInt("select id from EMPRESA where nome = ?", name);}
    public static int getIdTipoFromAtivo(String ativo) throws SQLException{
        return pstQueryResInt("select activotipo.id from (ACTIVO join activotipo on tipo = activotipo.id) where activo.id = ?", ativo);
    }

    // partimos do pressuposto que o id nunca irá ultrapassar a parte numérica
    private static String increaseId(String id){
        String nextId = id;
        char firstChar = nextId.charAt(0);
        nextId = nextId.substring(1);
        int numPart = Integer.parseInt(nextId);
        nextId = firstChar + fillZeros(numPart+1);
        return nextId;
    }

    private static String fillZeros(int number){
        String numb = String.valueOf(number);
        String newId = "";
        if(numb.length()> SIZE_NUMBER_PART_ID) return "";
        for(int n = numb.length(); n< SIZE_NUMBER_PART_ID; n++){
            newId += "0";
        }return newId += String.valueOf(number);
    }

    // estabelecer ligação com DB
    private static void connect() throws SQLException {
        try {
            con = DriverManager.getConnection(URL, username, password);
        } catch (Exception e) { e.printStackTrace();}
    }

    // fechar ligação e limpar recursos
    private static void closeConnection() throws SQLException {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    public static boolean checkEquipasMin2Elements() throws SQLException {
        //true if DB follows restriction
        boolean conditionCheck = false;
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT equipa, count(equipa) FROM PESSOA group by equipa order by equipa");
            while (rs.next()) {
                if(rs.getInt(2)>=2) {conditionCheck = true;
                }else {
                    conditionCheck = false;
                    break;
                }
            }
        } catch (SQLException e) { e.printStackTrace();
        } finally {closeConnection(); }
        return conditionCheck;
    }

    public static boolean checkEquipasMinXElements(int equipa, int numElem) throws SQLException {
        //true if DB follows restriction
        boolean conditionCheck = false;
        try {
            connect();
            pstmt = con.prepareStatement("SELECT count(equipa) FROM PESSOA where equipa = ? group by equipa order by equipa");
            pstmt.setInt(1, equipa);
            rs = pstmt.executeQuery();
            if(rs.next() && rs.getInt(1)>=numElem) conditionCheck = true;
        } catch (SQLException e) { e.printStackTrace();
        } finally {closeConnection(); }
        return conditionCheck;
    }

    public static void checkRestrictionVComercial() throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select distinct on (id) id, dtaquisicao, dtvcomercial from " +
                    "(ACTIVO join vcomercial on ACTIVO.id=VCOMERCIAL.activo) group by id, nome,valor,dtvcomercial " +
                    "order by id, dtvcomercial asc");
            List<String> idsAtivos = new ArrayList<>();
            List<Date> dataAq = new ArrayList<>();
            List<Date> primeiraDataVCom = new ArrayList<>();
            while (rs.next()) {
                idsAtivos.add(rs.getString(1));
                dataAq.add(rs.getDate(2));
                primeiraDataVCom.add(rs.getDate(3));
            }
            for(int n=0; n < idsAtivos.size();n++) {
                if(dataAq.get(n).compareTo(primeiraDataVCom.get(n))>0){
                    updateVComercial(dataAq.get(n),primeiraDataVCom.get(n),idsAtivos.get(n));
                }
            }
        } catch (SQLException e) { e.printStackTrace();
        } finally {closeConnection(); }
    }

    //correção RestrictionVComercial
    private static void updateVComercial(Date dtvSet, Date dtvWhere, String activo) throws SQLException{
        //correção consiste em igualar a dtvcomercial à dt de aquisição do ativo
        try {
            connect();
            pstmt = con.prepareStatement("update vcomercial set dtvcomercial = ? where dtvcomercial = ? and activo = ?");
            pstmt.setDate(1, dtvSet);
            pstmt.setDate(2, dtvWhere);
            pstmt.setString(3, activo);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    public static boolean checkRestrictionVComercial(String id) throws SQLException {
        //true if DB follows restriction
        boolean conditionCheck = false;
        try {
            connect();
            pstmt = con.prepareStatement("select distinct on (id) dtaquisicao, dtvcomercial from " +
                    "(ACTIVO join vcomercial on ACTIVO.id=VCOMERCIAL.activo) where id = ? group by id, nome,valor,dtvcomercial " +
                    "order by id, dtvcomercial asc");
            pstmt.setString(1, id);
            rs = pstmt.executeQuery();
            if(rs.next() && rs.getDate(1).compareTo(rs.getDate(2)) <= 0) conditionCheck = true;
        } catch (SQLException e) { e.printStackTrace();
        } finally {closeConnection(); }
        return conditionCheck;
    }

    public static void checkRestrictionIntervencao() throws SQLException {
        String custoInt = "select distinct on (id) valcusto from (ACTIVO inner join Intervencao on id=activo) where id = ? group by id, valcusto, noint order by id, valcusto desc";
        String valCom = "select distinct on (activo) valor from vcomercial where activo = ? group by activo, dtvcomercial, valor order by activo, dtvcomercial desc";
        List<String> ativos = getIdAtivos();
        for (String s : ativos) {
            //obter a intervencao mais cara
            int custoIntervencao = pstQueryResInt(custoInt,s);
            //obter valor comercial atual
            int valorComercial = pstQueryResInt(valCom,s);
            //se custoIntervencao>valorComercial DB não está conforme a presente restrição
            if(custoIntervencao>valorComercial){
                //correção da DB
                pstUpdate("update intervencao set dtfim = current_date where activo = ?", s);
                pstUpdate("update intervencao set estado = 'concluído' where activo = ?", s);
            }
        }
    }

    public static void checkRestrictionIntervencaoDtFim() throws SQLException {
        //não retorna nenhum valor, pois verifica a restrição e corrige a automaticamente
        List<Integer> interv = stQueryResListInt("select noint from intervencao where dtfim is not NULL");
        for (int s: interv){
            if(!pstQueryResString("select estado from intervencao where noint = ?", s).equals("concluído"))
                pstUpdate("update intervencao set estado = 'concluído' where noint = ?",s);
        }
    }

    public static void checkRestrictionAtivoHierarchy() throws SQLException {
        //não retorna nenhum valor, pois verifica a restrição e corrige a automaticamente
        List<String> ativos = getIdAtivos();
        for (String s: ativos) {
            //obter ativoTopo
            String ativoTopo = pstQueryResString("select idactivotopo from ACTIVO where id = ?",s);
            //obter tipo de ambos os ativos
            int tipoChild = getIdTipoFromAtivo(s);
            int tipoParent = getIdTipoFromAtivo(ativoTopo);
            if(tipoChild!=tipoParent) {
                // retifica a DB conforme a restrição substituindo o id do ativo filho pelo de topo
                pstUpdate("update ACTIVO set tipo = ? where id = ?",tipoParent,s);
            }
        }
    }

    public static boolean checkRestrictionAtivoHierarchy(String ativo) throws SQLException {
        //true if DB follows restriction
        boolean conditionCheck = true;
        //obter ativoTopo
        String ativoTopo = pstQueryResString("select idactivotopo from ACTIVO where id = ?",ativo);
        //obter tipo de ambos os ativos
        int tipoChild = getIdTipoFromAtivo(ativo);
        int tipoParent = getIdTipoFromAtivo(ativoTopo);
        if(tipoChild!=tipoParent) {conditionCheck = false;}
        return conditionCheck;
    }

    public static void checkRestrictionConflictGestaoMan() throws SQLException {
        List<Integer> equipas = getEquipas();
        List<Integer> gere = stQueryResListInt("select distinct on (pessoa) pessoa from ACTIVO");
        for (int s: gere) {
            //obter equipa a que pessoa s pertence
            int equipa = pstQueryResInt("select equipa from PESSOA where id = ?",s);
            int difEquipa = equipas.get(0);
            if(difEquipa == equipa) difEquipa = equipas.get(1);
            //obter ativos geridos pela pessoa s
            List<String> geridos = pstQueryResListString("select id from ACTIVO where pessoa = ?",s);
            for(String t:geridos){
                String querry = "select noint from intervencao join inter_equipa on noint=intervencao where activo = ? and equipa = ?";
                List<Integer> interv = pstQueryResListInt(querry,t,equipa);
                for (int n: interv){
                    //coreção de db consiste na troca de equipa, basta uma equipa diferente da atual
                    pstUpdate("update inter_equipa set equipa = ? where intervencao = ?",difEquipa,n);
                }
            }
        }
    }

    public static void novoAtivo(String nome, String dt, String marca, String modelo, String local, String idAtivoTp, int tipo, int empresa, int pessoa) throws SQLException {
        String newId = increaseId(stQueryResString("SELECT MAX(id) FROM ACTIVO"));
        try {
            connect();
            pstmt = con.prepareStatement("INSERT INTO ACTIVO (id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa) " +
                    "VALUES (?,?,?::bit,?,?,?,?,?,?,?,?)");
            pstmt.setString(1, newId);
            pstmt.setString(2,nome);
            //passamos estado = 1, pois definimos que este é o valor default do mesmo aquando da inserção de um novo ATIVO
            pstmt.setInt(3,1);
            pstmt.setDate(4, Date.valueOf(dt));
            pstmt.setString(5,marca);
            pstmt.setString(6,modelo);
            pstmt.setString(7,local);
            pstmt.setString(8,idAtivoTp);
            pstmt.setInt(9,tipo);
            pstmt.setInt(10,empresa);
            pstmt.setInt(11,pessoa);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public static void substituirElem(int idToReplaceOut, int idToReplaceIn) throws SQLException {
        int equipa = pstQueryResInt("SELECT equipa FROM PESSOA WHERE id=?",idToReplaceOut);
        // verificação se a equipa do elemento que vai substituir vai ter o número mínimo de elementos
        // verificamos se a
        if(!checkEquipasMinXElements(pstQueryResInt("SELECT equipa FROM PESSOA WHERE id=?",idToReplaceIn),3)){
            System.out.println("Não é possível substituir pessoa");
            System.out.println("Equipa de elemento que iria substituir, seria reduzida a menos de 2 elementos");
            return;
        }
        pstUpdate("update PESSOA set equipa = null where id = ?",idToReplaceOut);
        pstUpdate("update PESSOA set equipa = ? where id = ?",equipa,idToReplaceIn);
    }

    public static void ativoForaServico(String idActivo) throws SQLException {
        //ativo fora de serviço estado = 0
        pstUpdate("update ACTIVO set estado = '0' where id = ?",idActivo);
        //limpar intervenções passar para concluído
        pstUpdate("update intervencao set estado = 'concluído' where activo = ?",idActivo);
    }

    //partimos do pressuposto que o activo tem um valor comercial estipulado na data de aquisição
    public static int custoTotalAtivo(String id) throws SQLException {
        int custoAquisicao = pstQueryResInt("select distinct on (id) valor " +
                "from (ACTIVO inner join VCOMERCIAL on id = VCOMERCIAL.activo) where id = ? " +
                "group by id, nome,valor,dtvcomercial order by id, dtvcomercial asc",id);
        int custoIntervencao = pstQueryResInt("select sum(valcusto) from (INTERVENCAO right outer join ACTIVO" +
                " on INTERVENCAO.activo = ACTIVO.id) where id = ?", id);
        return custoAquisicao + custoIntervencao;
    }

    public static void query2d(String id) throws SQLException {
        pstQueryResPrint("select ACTIVO.nome, PESSOA.equipa, PESSOA.nome from " +
                "(PESSOA full outer join INTER_EQUIPA " +
                "on PESSOA.equipa=INTER_EQUIPA.equipa full outer join INTERVENCAO " +
                "on INTER_EQUIPA.intervencao=INTERVENCAO.noint left outer join ACTIVO " +
                "on INTERVENCAO.activo=ACTIVO.id) where ((INTERVENCAO.estado " +
                "IN ('em execução','em análise')) and ACTIVO.nome = ?) " +
                "union select ACTIVO.nome, PESSOA.equipa, PESSOA.nome from (PESSOA full outer join ACTIVO " +
                "on PESSOA.id=ACTIVO.pessoa) where ACTIVO.nome = ?",id,2,3,"ativo  equipa  pessoa");
    }

    public static void query2e(String nome) throws SQLException {
        pstQueryResPrint("select A.nome from (ACTIVO as A inner join PESSOA as P on pessoa = P.id) where" +
                " P.nome = ? union select ACTIVO.nome as nomeInterv from " +
                "(ACTIVO inner join INTERVENCAO on ACTIVO.id = INTERVENCAO.activo inner join " +
                "INTER_EQUIPA on INTERVENCAO.noint = INTER_EQUIPA.intervencao inner join PESSOA " +
                "on INTER_EQUIPA.equipa = PESSOA.equipa) where PESSOA.nome =?", nome,2,1,"ativo");
    }

    public static void query3c() throws SQLException {
        stQueryResPrint("select DISTINCT PESSOA.nome, profissao, telefone from (PESSOA " +
                "left outer join TEL_PESSOA on PESSOA.id=TEL_PESSOA.pessoa inner join ACTIVO " +
                "on PESSOA.id=ACTIVO.pessoa)",3,"pessoa  profissão  telefone");
    }

    public static void query3d(String period, int amount) throws SQLException {
        String intervalMin = "" + amount + " " + period;
        amount++;
        String intervalMax = "" + amount + " " + period;
        try {
            connect();
            pstmt = con.prepareStatement("select noint from INTERVENCAO where dtinicio between " +
                    "date_trunc(?, CURRENT_DATE + ?::interval) and " +
                    "date_trunc(?, CURRENT_DATE + ?::interval)");
            pstmt.setString(1, period);
            pstmt.setString(2, intervalMin);
            pstmt.setString(3, period);
            pstmt.setString(4, intervalMax);
            rs = pstmt.executeQuery();
            App.printTable(rs,1, "Número Intervenção");
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static String stQueryResString(String query) throws SQLException {
        String res = "";
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) res = rs.getString(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static void stQueryResPrint(String query,int numCols, String header) throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            App.printTable(rs,numCols,header);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static int pstQueryResInt(String query, String val) throws SQLException {
        int res = -1;
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, val);
            rs = pstmt.executeQuery();
            if(rs.next()) res = rs.getInt(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static int pstQueryResInt(String query, int val) throws SQLException {
        int res = -1;
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, val);
            rs = pstmt.executeQuery();
            if(rs.next()) res = rs.getInt(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static String pstQueryResString(String query, String val) throws SQLException {
        String res = "";
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, val);
            rs = pstmt.executeQuery();
            if(rs.next()) res = rs.getString(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static String pstQueryResString(String query, int val) throws SQLException {
        String res = "";
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, val);
            rs = pstmt.executeQuery();
            if(rs.next()) res = rs.getString(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static void pstQueryResPrint(String query, String val, int numEqualFields, int numCols, String header) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            for(int n=1;n<=numEqualFields;n++)pstmt.setString(n, val);
            rs = pstmt.executeQuery();
            App.printTable(rs,numCols, header);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    public static List<String> pstQueryResListString(String query, int val) throws SQLException {
        List<String> res = new ArrayList<>();
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, val);
            rs = pstmt.executeQuery();
            for(int n=0; rs.next(); n++) {res.add(rs.getString(1));}
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    public static List<String> stQueryResListString(String query) throws SQLException {
        List<String> res = new ArrayList<>();
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            for(int n=0; rs.next(); n++) {res.add(rs.getString(1));}
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    public static List<Integer> pstQueryResListInt(String query, String val1, int val2) throws SQLException {
        List<Integer> res = new ArrayList<>();
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, val1);
            pstmt.setInt(2, val2);
            rs = pstmt.executeQuery();
            for(int n=0; rs.next(); n++) {res.add(rs.getInt(1));}
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    public static List<Integer> stQueryResListInt(String query) throws SQLException {
        List<Integer> res = new ArrayList<>();
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            for(int n=0; rs.next(); n++) {res.add(rs.getInt(1));}
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static void pstUpdate(String query, String val) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setString(1, val);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static void pstUpdate(String query, int val) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, val);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static void pstUpdate(String query, int firstVal,  String secondVal) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, firstVal);
            pstmt.setString(2, secondVal);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static void pstUpdate(String query, int firstVal,  int secondVal) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, firstVal);
            pstmt.setInt(2, secondVal);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }
}
