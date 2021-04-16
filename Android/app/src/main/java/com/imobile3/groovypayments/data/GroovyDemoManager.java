package com.imobile3.groovypayments.data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.imobile3.groovypayments.MainApplication;
import com.imobile3.groovypayments.data.dao.CartDao;
import com.imobile3.groovypayments.data.dao.CartProductDao;
import com.imobile3.groovypayments.data.dao.ProductDao;
import com.imobile3.groovypayments.data.dao.ProductTaxJunctionDao;
import com.imobile3.groovypayments.data.dao.TaxDao;
import com.imobile3.groovypayments.data.dao.UserDao;
import com.imobile3.groovypayments.data.entities.CartEntity;
import com.imobile3.groovypayments.data.entities.CartProductEntity;
import com.imobile3.groovypayments.data.entities.ProductEntity;
import com.imobile3.groovypayments.data.entities.ProductTaxJunctionEntity;
import com.imobile3.groovypayments.data.entities.TaxEntity;
import com.imobile3.groovypayments.data.entities.UserEntity;
import com.imobile3.groovypayments.data.enums.GroovyColor;
import com.imobile3.groovypayments.data.enums.GroovyIcon;
import com.imobile3.groovypayments.data.utils.ProductBuilder;

import java.util.ArrayList;
import java.util.List;

public final class GroovyDemoManager {

    private static final String TAG = GroovyDemoManager.class.getSimpleName();

    //region Singleton Implementation

    private static GroovyDemoManager sInstance;

    private GroovyDemoManager() {
    }

    @NonNull
    public static synchronized GroovyDemoManager getInstance() {
        if (sInstance == null) {
            sInstance = new GroovyDemoManager();
        }
        return sInstance;
    }

    //endregion

    /**
     * Delete the current database instance (potentially dangerous operation!)
     * and populate a new instance with fresh demo data.
     */
    public void resetDatabase(
            @NonNull final ResetDatabaseCallback callback) {
        new ResetDatabaseTask(MainApplication.getInstance(), callback)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface ResetDatabaseCallback {

        void onDatabaseReset();
    }

    private class ResetDatabaseTask extends AsyncTask<Void, Void, Void> {

        @NonNull
        private final Context mContext;
        @NonNull
        private final ResetDatabaseCallback mCallback;

        private ResetDatabaseTask(
                @NonNull final Context context,
                @NonNull final ResetDatabaseCallback callback) {
            mContext = context;
            mCallback = callback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Blow away any existing database instance.
            DatabaseHelper.getInstance().eraseDatabase(mContext);

            // Initialize a new database instance.
            DatabaseHelper.getInstance().init(mContext);


            // Insert entities into database instance.
            GroovyDatabase db = DatabaseHelper.getInstance().getDatabase();

            List<ProductEntity> productEntities = addProductEntities(db.getProductDao());

            List<TaxEntity> taxEntities = addTaxEntities(db.getTaxDao());

            addProductTaxJunctionEntities(db.getProductTaxJunctionDao(), taxEntities, productEntities);

            List<CartEntity> cartEntities = addCartEntities(db.getCartDao());

            for (CartEntity cartEntity : cartEntities) {
                addCartProductEntities(db.getCartProductDao(), cartEntity);
            }

            //
            addAuthorisedUsers(db.getUserDao());

            // All done!
            return null;
        }

        private List<ProductEntity> addProductEntities(ProductDao dao) {
            List<ProductEntity> productEntities = new ArrayList<>();

            // Add one product.
            productEntities.add(ProductBuilder.build(101L,
                    "Tasty Chicken Sandwich",
                    "Chicken, lettuce, tomato and mayo",
                    750L, 200L,
                    GroovyIcon.Sandwich, GroovyColor.Yellow));
            productEntities.addAll(TestDataRepository.getInstance()
                    .getProducts(TestDataRepository.Environment.GroovyDemo));
            dao.insertProducts(
                    productEntities.toArray(new ProductEntity[0]));
            return productEntities;
        }

        private void addProductTaxJunctionEntities(ProductTaxJunctionDao dao, List<TaxEntity> taxEntities, List<ProductEntity> productEntities) {
            for (ProductEntity productEntity : productEntities) {
                List<ProductTaxJunctionEntity> list = TestDataRepository.getInstance().getProductTaxJunctions(productEntity, taxEntities);
                dao.insertProductTaxJunctions(list.toArray(new ProductTaxJunctionEntity[0]));
            }
        }

        private List<TaxEntity> addTaxEntities(TaxDao dao) {
            List<TaxEntity> list = TestDataRepository.getInstance().getTaxes(TestDataRepository.Environment.GroovyDemo);
            dao.insertTaxes(list.toArray(new TaxEntity[0]));
            return list;
        }

        private List<CartEntity> addCartEntities(CartDao dao) {
            List<CartEntity> list = TestDataRepository.getInstance().getCarts(TestDataRepository.Environment.GroovyDemo);
            dao.insertCarts(list.toArray(new CartEntity[0]));
            return list;
        }

        private void addCartProductEntities(CartProductDao dao, CartEntity entity) {
            List<CartProductEntity> list = TestDataRepository.getInstance().getCartProducts(TestDataRepository.Environment.GroovyDemo, entity);
            dao.insertCartProducts(list.toArray(new CartProductEntity[0]));
        }

        private void addAuthorisedUsers(UserDao dao) {
            List<UserEntity> list = TestDataRepository.getInstance().getAuthorisedUsers(TestDataRepository.Environment.GroovyDemo);
            dao.insertUsers(list.toArray(new UserEntity[0]));
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mCallback.onDatabaseReset();
        }
    }
}
