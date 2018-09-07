package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.ServiceContract
import com.template.ServiceState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

@InitiatedBy(RequestServiceHistoryFlow::class)
@StartableByRPC
class ProvideServiceHistoryFlow(val session: FlowSession) : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): Unit {

        val serviceStateRef = serviceHub.vaultService.queryBy<ServiceState>().states.first()

        session.send(serviceStateRef.state.data)
    }
}