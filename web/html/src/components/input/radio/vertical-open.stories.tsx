import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Radio } from "./Radio";

export default () => {
  const model = {
    level: "beginner",
  };

  return (
    <>
      <p>Vertical with open option:</p>
      <Form
        model={model}
        onChange={(newModel) => {
          model["level"] = newModel["level"];
        }}
        onSubmit={() => Loggerhead.info(model)}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <Radio
          name="level"
          label={t("Level")}
          required
          openOption={true}
          labelClass="col-md-3"
          divClass="col-md-6"
          items={[
            { label: t("Beginner"), value: "beginner" },
            { label: t("Normal"), value: "normal" },
            { label: t("Expert"), value: "expert" },
          ]}
        />
        <SubmitButton className="btn-primary" text={t("Submit")} />
      </Form>
    </>
  );
};
