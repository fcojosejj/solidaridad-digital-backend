package com.fjjj0001.tfg.solidaridad_digital.util

import org.hibernate.boot.model.TypeContributions
import org.hibernate.dialect.MySQLDialect
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.SqlTypes.VARBINARY
import org.hibernate.type.descriptor.sql.internal.DdlTypeImpl

// Custom dialect that allows the use of mediumblob with MySQL but uses the default BLOB type with H2 testing database
class CustomMySQLDialect : MySQLDialect() {
    override fun registerColumnTypes(typeContributions: TypeContributions, serviceRegistry: ServiceRegistry?) {
        super.registerColumnTypes(typeContributions, serviceRegistry)
        val ddlTypeRegistry = typeContributions.typeConfiguration.ddlTypeRegistry
        ddlTypeRegistry.addDescriptor(
            DdlTypeImpl(
                VARBINARY,
                "mediumblob",
                this
            )
        )
    }
}