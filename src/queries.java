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

    public static void test() throws SQLException {
        query2d("válvula de ar condicionado");
        query2e("Manuel Fernandes");
        query3c();
        //query3d();
        //System.out.println(checkEquipasMin2Elements());
        //System.out.println(getEquipaFromId(1));
        //substituirElem(1,3);
        //novoActivo("test","2020-02-03",null,null,"Lisboa","Z0005",1,1,1);
        //activoForaServico("a0001");
        //custoTotalActivo("a0001");
    }

    public static List<String> getEmpresas() throws SQLException {
        List<String> empresas = new ArrayList<String>();
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery("select nome from EMPRESA");
            for(int n=0; rs.next(); n++) {
                empresas.add(rs.getString(1));
                System.out.println(rs.getString(1));
            }
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return empresas;
    }

    public static int getIdEmpresa(String name) throws SQLException{
        return pstQuerryResInt("select id from EMPRESA where nome = ?", name);
    }

    // partimos do pressuposto que o id nunca irá ultrapassar a parte numerica
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

    public static void checkRestrictionActivohierarchy() {

    }

    public static void checkRestrictionIntervencaoDtFim() {

    }

    public static void checkRestrictionIntervencao() {

    }

    public static void checkRestrictionVcomercial() {

    }



    public static void checkRestrictionConflictGestaoMan() {

    }

    //passamos estado = 1, pois definimos que este é o valor dafault do mesmo aquando da inserção de um novo ACTIVO
    public static void novoActivo(String nome, String dt, String marca, String modelo, String local, String idactivotp, int tipo, int empresa, int pessoa) throws SQLException {
        String newId = increaseId(stQueryResString("SELECT MAX(id) FROM ACTIVO"));
        try {
            connect();
            pstmt = con.prepareStatement("INSERT INTO ACTIVO (id, nome, estado, dtaquisicao, marca, modelo, localizacao, idactivotopo, tipo, empresa, pessoa) " +
                    "VALUES (?,?,?::bit,?,?,?,?,?,?,?,?)");
            pstmt.setString(1, newId);
            pstmt.setString(2,nome);
            pstmt.setInt(3,1);
            pstmt.setDate(4, Date.valueOf(dt));
            pstmt.setString(5,marca);
            pstmt.setString(6,modelo);
            pstmt.setString(7,local);
            pstmt.setString(8,idactivotp);
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

    // vai ser pedido os nomes

    public static void substituirElem(int idToReplaceOut, int idToReplaceIn) throws SQLException {
        int equipa = getEquipaFromId(idToReplaceOut);
        // verificação se a equipa do elemento que vai substituir vai ter o número mínimo de elementos
        // verificamos se a
        if(!checkEquipasMinXElements(getEquipaFromId(idToReplaceIn),3)){
            System.out.println("Can't replace elements");
            System.out.println("Team from element that would replace, would have less than 2 elements");
            return;
        }
        pstUpdate("update PESSOA set equipa = null where id = ?",idToReplaceOut);
        updateEquipaElem(idToReplaceIn,equipa);
    }

    private static int getEquipaFromId(int id) throws SQLException {
        int equipa = -1;
        try {
            connect();
            pstmt = con.prepareStatement("SELECT equipa FROM PESSOA WHERE id=?");
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if(rs.next()) equipa = rs.getInt(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return equipa;
    }

    private static void updateEquipaElem(int id, int equipa) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement("update PESSOA set equipa = ? where id = ?");
            pstmt.setInt(1, equipa);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    //activo fora de serviço == estado = 0?
    //adicionar restrição ACTIVO estado=0 n sofre intervenções
    //limpar intervenções? passar para concluido
    public static void activoForaServico(String idActivo) throws SQLException {
        pstUpdate("update ACTIVO set estado = '0' where id = ?",idActivo);
    }

    //partimos do pressuposto que o activo tem um valor comercial estipulado na data de aquisição
    public static void custoTotalActivo(String id) throws SQLException {
        int custoAquisicao = pstQuerryResInt("select distinct on (id) valor\n" +
                "from (ACTIVO inner join VCOMERCIAL on id = VCOMERCIAL.activo) where id = ?\n" +
                "group by id, nome,valor,dtvcomercial order by id, dtvcomercial asc",id);
        int custoIntervencao = pstQuerryResInt("select sum(valcusto) from (INTERVENCAO right outer join ACTIVO" +
                " on INTERVENCAO.activo = ACTIVO.id) where id = ?", id);
        int custoTotal = custoAquisicao + custoIntervencao;
        System.out.println(custoAquisicao + " " + custoIntervencao + " " + custoTotal);
    }

    public static void query2d(String id) throws SQLException {
        pstQuerryResPrint("select ACTIVO.nome, PESSOA.equipa, PESSOA.nome from " +
                "(PESSOA full outer join INTER_EQUIPA " +
                "on PESSOA.equipa=INTER_EQUIPA.equipa full outer join INTERVENCAO " +
                "on INTER_EQUIPA.intervencao=INTERVENCAO.noint left outer join ACTIVO " +
                "on INTERVENCAO.activo=ACTIVO.id) where ((INTERVENCAO.estado " +
                "IN ('em execução','em análise')) and ACTIVO.nome = ?) " +
                "union select ACTIVO.nome, PESSOA.equipa, PESSOA.nome from (PESSOA full outer join ACTIVO " +
                "on PESSOA.id=ACTIVO.pessoa) where ACTIVO.nome = ?",id,2,3);
    }

    public static void query2e(String nome) throws SQLException {
        pstQuerryResPrint("select A.nome from (ACTIVO as A inner join PESSOA as P on pessoa = P.id) where" +
                " P.nome = ? union select ACTIVO.nome as nomeInterv from " +
                "(ACTIVO inner join INTERVENCAO on ACTIVO.id = INTERVENCAO.activo inner join " +
                "INTER_EQUIPA on INTERVENCAO.noint = INTER_EQUIPA.intervencao inner join PESSOA " +
                "on INTER_EQUIPA.equipa = PESSOA.equipa) where PESSOA.nome =?", nome,2,1);
    }

    public static void query3c() throws SQLException {
        stQueryResPrint("select DISTINCT PESSOA.nome, profissao, telefone from (PESSOA " +
                "left outer join TEL_PESSOA on PESSOA.id=TEL_PESSOA.pessoa inner join ACTIVO " +
                "on PESSOA.id=ACTIVO.pessoa)",3);

    }

    public static void query3d() throws SQLException {
        /*try {
            connect();
            pstmt = con.prepareStatement("select noint from (INTERVENCAO join INTER_EQUIPA " +
                    "on INTERVENCAO.noint=INTER_EQUIPA.intervencao) where dtinicio between " +
                    "date_trunc('month', CURRENT_DATE + interval '1 month' ) and date_trunc('month', " +
                    "CURRENT_DATE + interval '2 month')");
            pstmt.setString(1, val);
            rs = pstmt.executeQuery();
            App.printTable(rs,2);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}*/
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

    private static int stQueryResInt(String query) throws SQLException {
        int res = -1;
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()) res = rs.getInt(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static void stQueryResPrint(String query,int numCols) throws SQLException {
        try {
            connect();
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            App.printTable(rs,numCols);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static int pstQuerryResInt(String query, String val) throws SQLException {
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

    private static int pstQuerryResInt(String query, int val) throws SQLException {
        int res = -1;
        try {
            connect();
            pstmt = con.prepareStatement(query);
            pstmt.setInt(1, val);
            pstmt.executeQuery();
            if(rs.next()) res = rs.getInt(1);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
        return res;
    }

    private static void pstQuerryResPrint(String query, String val, int numEqualFields, int numCols) throws SQLException {
        try {
            connect();
            pstmt = con.prepareStatement(query);
            for(int n=1;n<=numEqualFields;n++)pstmt.setString(n, val);
            rs = pstmt.executeQuery();
            App.printTable(rs,numCols);
        } catch (SQLException e) {e.printStackTrace();
        } finally { closeConnection();}
    }

    private static String pstQuerryResString(String query, String val) throws SQLException {
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
}
