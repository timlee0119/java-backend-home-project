package com.test.bank.service;

import com.test.bank.db.tables.records.UserRecord;
import com.test.bank.initializer.DataSourceInitializer;
import com.test.bank.model.User;
import com.test.bank.model.TransferResponse;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.types.UInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.test.bank.db.Tables.USER;

@Singleton
public class TransactionService {

    DefaultConfiguration jooqConfiguration;

    @Inject
    public TransactionService(DataSourceInitializer dataSourceInitializer) {
        this.jooqConfiguration = dataSourceInitializer.getJooqConfiguration();
    }

    public TransferResponse transfer(int fromUserId, int toUserId, int amount) {
        // TODO implement transfer
        if (fromUserId == toUserId || amount <= 0) {
            return null;
        }
        UInteger fromUserId_ = UInteger.valueOf(fromUserId);
        UInteger toUserId_ = UInteger.valueOf(toUserId);
        TransferResponse transferResponse = null;
        DSLContext ctx = DSL.using(jooqConfiguration);

        // do transfer only if both users exist and fromUser's wallet is enough
        UserRecord fromUser = ctx.fetchOne(USER, USER.ID.eq(fromUserId_));
        UserRecord toUser = ctx.fetchOne(USER, USER.ID.eq(toUserId_));
        if (fromUser != null && toUser != null && fromUser.getWallet() >= amount) {
            ctx.transaction(ctx_ -> {
                DSL.using(ctx_)
                        .update(USER)
                        .set(USER.WALLET, USER.WALLET.minus(amount))
                        .where(USER.ID.eq(fromUserId_))
                        .execute();
                DSL.using(ctx_)
                        .update(USER)
                        .set(USER.WALLET, USER.WALLET.plus(amount))
                        .where(USER.ID.eq(toUserId_))
                        .execute();
            });

            transferResponse = new TransferResponse();
            transferResponse.setFromUser(ctx.fetchOne(USER, USER.ID.eq(fromUserId_)).into(User.class));
            transferResponse.setToUser(ctx.fetchOne(USER, USER.ID.eq(toUserId_)).into(User.class));
        } else {
//            System.out.println("Invalid user id.");
        }
        return transferResponse;
    }

}
