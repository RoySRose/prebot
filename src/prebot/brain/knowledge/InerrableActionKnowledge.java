package prebot.brain.knowledge;

import prebot.common.code.Code.KnowledgeActionEvaluation;

public abstract class InerrableActionKnowledge extends ActionKnowledge {

	@Override
	protected final void prevent() {}

	@Override
	protected final boolean doSomeOtherthing() {
		return false;
	}

	@Override
	protected final KnowledgeActionEvaluation evaulateAction() {
		return KnowledgeActionEvaluation.GOOD_ACTION;
	}

	@Override
	public final boolean notOccured() {
		return false;
	}

	@Override
	public final boolean occured() {
		return true;
	}

	@Override
	public final boolean foundCertainProof() {
		return true;
	}

	@Override
	public final boolean foundCertainDisproof() {
		return false;
	}
}
