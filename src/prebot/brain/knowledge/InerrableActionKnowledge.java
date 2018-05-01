package prebot.brain.knowledge;

import prebot.common.code.Code.KnowledgeActionEvaluation;

public abstract class InerrableActionKnowledge extends ActionKnowledge {

	@Override
	protected void prevent() {}

	@Override
	protected boolean doSomeOtherthing() {
		return false;
	}

	@Override
	protected KnowledgeActionEvaluation evaulateAction() {
		return KnowledgeActionEvaluation.GOOD_ACTION;
	}

	@Override
	public boolean notOccured() {
		return false;
	}

	@Override
	public boolean occured() {
		return true;
	}

	@Override
	public boolean foundCertainProof() {
		return true;
	}

	@Override
	public boolean foundCertainDisproof() {
		return false;
	}
}
