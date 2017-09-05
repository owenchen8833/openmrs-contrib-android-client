package org.openmrs.mobile.data.db.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.SQLOperator;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;

import org.openmrs.mobile.data.db.AppDatabase;
import org.openmrs.mobile.data.db.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

public class RepositoryImpl implements Repository {
	@Inject
	public RepositoryImpl() { }

	@Override
	public <M> M querySingle(@NonNull ModelAdapter<M> table, @NonNull SQLOperator... operators) {
		checkNotNull(table);
		checkNotNull(operators);

		return SQLite.select()
				.from(table.getModelClass())
				.where(operators)
				.querySingle();
	}

	@Override
	public <T, M> T querySingle(@NonNull Class<T> cls, @NonNull ModelAdapter<M> table,
			@NonNull IProperty property, @Nullable SQLOperator... operators) {
		checkNotNull(cls);
		checkNotNull(table);
		checkNotNull(property);


		ModelQueriable<M> query = SQLite.select(property).from(table.getModelClass());

		if (operators != null) {
			query = ((From<M>) query).where(operators);
		}

		return query.queryCustomSingle(cls);
	}

	@Override
	public <M> List<M> query(@NonNull ModelAdapter<M> table, @Nullable SQLOperator... operators) {
		checkNotNull(table);

		ModelQueriable<M> query = SQLite.select()
				.from(table.getModelClass());

		if (operators != null) {
			query = ((From<M>) query).where(operators);
		}

		return query.queryList();
	}

	@Override
	public <M> List<M> query(@NonNull ModelQueriable<M> query) {
		checkNotNull(query);

		return query.queryList();
	}

	@Override
	public <T, M> List<T> queryCustom(@NonNull Class<T> cls, @NonNull ModelAdapter<M> table, @NonNull IProperty property,
			@Nullable SQLOperator... operators) {
		checkNotNull(property);

		List<IProperty> list = new ArrayList<>(1);
		list.add(property);

		return queryCustom(cls, table, list, operators);
	}

	@Override
	public <T, M> List<T> queryCustom(@NonNull Class<T> cls, @NonNull ModelAdapter<M> table,
			@NonNull Collection<IProperty> properties,
			@Nullable SQLOperator... operators) {
		checkNotNull(cls);
		checkNotNull(table);
		checkNotNull(properties);

		ModelQueriable<M> query = SQLite.select((IProperty[])properties.toArray())
				.from(table.getModelClass());

		if (operators != null) {
			query = ((From<M>) query).where(operators);
		}

		return query.queryCustomList(cls);
	}

	@Override
	public <T, M> List<T> queryCustom(@NonNull Class<T> cls, @NonNull ModelQueriable<M> query) {
		checkNotNull(cls);
		checkNotNull(query);

		return query.queryCustomList(cls);
	}

	@Override
	public <M> long count(@NonNull ModelAdapter<M> table, @Nullable SQLOperator... operators) {
		checkNotNull(table);

		ModelQueriable<M> query = SQLite.selectCountOf()
				.from(table.getModelClass());

		if (operators != null) {
			query = ((From<M>) query).where(operators);
		}

		return query.count();
	}

	@Override
	public <M> long count(@NonNull ModelQueriable<M> query) {
		checkNotNull(query);

		return query.count();
	}

	@Override
	public <M> boolean save(@NonNull ModelAdapter<M> table, @NonNull M model) {
		checkNotNull(table);
		checkNotNull(model);

		return table.save(model);
	}

	@Override
	public <M> void saveAll(@NonNull ModelAdapter<M> table, @NonNull List<M> models) {
		checkNotNull(table);
		checkNotNull(models);

		FlowManager.getDatabase(AppDatabase.class).executeTransaction(
				FastStoreModelTransaction
						.saveBuilder(table)
						.addAll(models)
						.build()
		);
	}

	@Override
	public <M> boolean delete(@NonNull ModelAdapter<M> table, @NonNull M model) {
		checkNotNull(table);
		checkNotNull(model);

		return table.delete(model);
	}

	@Override
	public <M> void deleteAll(@NonNull ModelAdapter<M> table, @NonNull List<M> models) {
		checkNotNull(table);
		checkNotNull(models);

		FlowManager.getDatabase(AppDatabase.class).executeTransaction(
				FastStoreModelTransaction
						.deleteBuilder(table)
						.addAll(models)
						.build()
		);
	}

	@Override
	public <M> void deleteAll(@NonNull ModelAdapter<M> table, @Nullable SQLOperator... operators) {
		checkNotNull(table);

		ModelQueriable<M> query = SQLite.delete().from(table.getModelClass());

		if (operators != null) {
			query = ((From<M>) query).where(operators);
		}

		query.execute();
	}

	@Override
	public <M> void deleteAll(@NonNull ModelQueriable<M> query) {
		checkNotNull(query);

		query.execute();
	}
}

