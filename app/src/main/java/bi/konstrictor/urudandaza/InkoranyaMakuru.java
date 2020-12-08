package bi.konstrictor.urudandaza;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

import bi.konstrictor.urudandaza.models.Account;
import bi.konstrictor.urudandaza.models.ActionStock;
import bi.konstrictor.urudandaza.models.Cloture;
import bi.konstrictor.urudandaza.models.Liquide;
import bi.konstrictor.urudandaza.models.Remboursement;
import bi.konstrictor.urudandaza.models.Personne;
import bi.konstrictor.urudandaza.models.Produit;
import bi.konstrictor.urudandaza.models.ProxyAction;
import bi.konstrictor.urudandaza.models.Signature;

public class InkoranyaMakuru extends OrmLiteSqliteOpenHelper {
    private static final String DB_NAME = "ringtone.mp3";
    private static final int DB_VERSION = 1;
    Context context;

    public InkoranyaMakuru(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
//    public Dao<Produit, Integer> getDaoProduit() throws SQLException {
//        return getDao(Produit.class);
//    }
//    public Dao<Personne, Integer> getDaoPersonne() throws SQLException {
//        return getDao(Personne.class);
//    }
//    public Dao<Liquide, Integer> getDaoLiquide() throws SQLException {
//        return getDao(Liquide.class);
//    }
//    public Dao<Remboursement, Integer> getDaoDette() throws SQLException {
//        return getDao(Remboursement.class);
//    }
//    public Dao<ProxyAction, Integer> getDaoProxy() throws SQLException {
//        return getDao(ProxyAction.class);
//    }
//    public Dao<ActionStock, Integer> getDaoActionStock() throws SQLException {
//        return getDao(ActionStock.class);
//    }
//    public Dao<Cloture, Integer> getDaoCloture() throws SQLException {
//        return getDao(Cloture.class);
//    }
//    public Dao<Account, Integer> getDaoAccounts() throws SQLException {
//        return getDao(Account.class);
//    }
//    public Dao<Signature, Integer> getDaoSignatures() throws SQLException {
//        return getDao(Signature.class);
//    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, Produit.class);
            TableUtils.createTableIfNotExists(connectionSource, Personne.class);
            TableUtils.createTableIfNotExists(connectionSource, ActionStock.class);
            TableUtils.createTableIfNotExists(connectionSource, Cloture.class);
            TableUtils.createTableIfNotExists(connectionSource, Liquide.class);
            TableUtils.createTableIfNotExists(connectionSource, ProxyAction.class);
            TableUtils.createTableIfNotExists(connectionSource, Remboursement.class);
            TableUtils.createTableIfNotExists(connectionSource, Account.class);
            TableUtils.createTableIfNotExists(connectionSource, Signature.class);

            getDao(Cloture.class).create(new Cloture());

            database.execSQL("CREATE TRIGGER insertion_stock " +
                "AFTER INSERT ON actionstock FOR EACH ROW BEGIN " +
                "UPDATE produit SET quantite = quantite+NEW.quantite WHERE id = NEW.produit_id;" +
                "UPDATE cloture SET payee_achat=payee_achat+NEW.payee, achat=achat+NEW.total " +
                    "WHERE cloture.id=NEW.cloture_id AND NEW.quantite>0; " +
                "UPDATE cloture SET vente=vente+NEW.total, payee_vente=payee_vente+NEW.payee " +
                    "WHERE cloture.id=NEW.cloture_id AND NEW.quantite<0; " +
                "INSERT INTO proxyaction (produit_id, quantite, prix, total, payee, personne_id, " +
                        "motif, date, cloture_id, perimee) " +
                    "VALUES (NEW.produit_id, NEW.quantite, NEW.prix, NEW.total, NEW.payee, " +
                        "NEW.personne_id, NEW.motif, NEW.date, NEW.cloture_id, NEW.perimee); "+
                "END;");

            database.execSQL("CREATE TRIGGER modification_stock " +
                "AFTER UPDATE ON ActionStock FOR EACH ROW BEGIN " +
                "UPDATE Produit SET quantite = quantite-OLD.quantite+NEW.quantite WHERE id=NEW.produit_id; " +
                "UPDATE cloture SET " +
                    "achat=achat-OLD.total+NEW.total, payee_achat=payee_achat-OLD.payee+NEW.payee " +
                    "WHERE cloture.id=NEW.cloture_id AND NEW.quantite>0;  " +
                "UPDATE cloture SET " +
                    "vente = vente-OLD.total+NEW.total, payee_vente = payee_vente-OLD.payee+NEW.payee " +
                    "WHERE cloture.id=NEW.cloture_id AND NEW.quantite<0;  " +
                "DELETE FROM proxyaction WHERE date = OLD.date; "+
                "INSERT INTO proxyaction (produit_id, quantite, prix, total, payee, personne_id, " +
                        "motif, date, cloture_id, perimee)" +
                    "VALUES (NEW.produit_id, NEW.quantite, NEW.prix, NEW.total, NEW.payee, " +
                        "NEW.personne_id, NEW.motif, NEW.date, NEW.cloture_id, NEW.perimee); "+
                "END;");

            database.execSQL("create trigger suppression_stock " +
                "AFTER DELETE ON ActionStock FOR EACH ROW BEGIN " +
                "UPDATE Produit SET quantite = quantite-OLD.quantite " +
                    "WHERE id = OLD.produit_id; " +
                "UPDATE cloture SET achat = achat-OLD.total, payee_achat = payee_achat-OLD.payee " +
                    "WHERE cloture.id=OLD.cloture_id AND OLD.quantite>0; " +
                "UPDATE cloture SET vente = vente-OLD.total, payee_vente = payee_vente-OLD.payee " +
                    "WHERE cloture.id=OLD.cloture_id AND OLD.quantite<0; " +
                "DELETE FROM proxyaction WHERE id = OLD.id; "+
                "END;");

        } catch (Exception e) {
            Log.e("INKORANYAMAKURU", e.getMessage());
        }
    }

    public Cloture getLatestCloture(){
        try {
            List<Cloture> clotures = getDao(Cloture.class).queryBuilder()
                    .where().eq("compiled", false).query();
            if (clotures.size() > 1) {
                for (int i = 0; i < clotures.size() - 1; i++) {
                    Cloture cloture = clotures.get(i);
                    cloture.compiled = true;
                    getDao(Cloture.class).update(cloture);
                }
                return clotures.get(clotures.size() - 1);
            } else if (clotures.size() == 1) {
                return clotures.get(0);
            } else {
                return getDao(Cloture.class).createIfNotExists(new Cloture());
            }
        }catch (SQLException e){
            Log.i("ERREUR", e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, "Hari ikintu kutagenze neza", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
    }
}
