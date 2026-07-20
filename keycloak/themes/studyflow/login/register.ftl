<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('firstName','email','username','password','password-confirm','cpf','birthDate','educationLevel'); section>
    <#if section = "header">
        ${msg("registerTitle")}
    <#elseif section = "form">
        <form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.registrationAction}" method="post">
            <div class="${properties.kcFormGroupClass!}">
                <label for="firstName" class="${properties.kcLabelClass!}">${msg("firstName")}</label>
                <input type="text" id="firstName" name="firstName" class="${properties.kcInputClass!}"
                       value="${(register.formData.firstName!'')}" required />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="email" class="${properties.kcLabelClass!}">${msg("email")}</label>
                <input type="email" id="email" name="email" class="${properties.kcInputClass!}"
                       value="${(register.formData.email!'')}" autocomplete="email" required />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="password" class="${properties.kcLabelClass!}">${msg("password")}</label>
                <input type="password" id="password" name="password" class="${properties.kcInputClass!}"
                       autocomplete="new-password" required />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="password-confirm" class="${properties.kcLabelClass!}">${msg("passwordConfirm")}</label>
                <input type="password" id="password-confirm" name="password-confirm" class="${properties.kcInputClass!}"
                       autocomplete="new-password" required />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="cpf" class="${properties.kcLabelClass!}">${msg("cpf")}</label>
                <input type="text" id="cpf" name="user.attributes.cpf" class="${properties.kcInputClass!}"
                       value="${(register.formData['user.attributes.cpf']!'')}" required />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="birthDate" class="${properties.kcLabelClass!}">${msg("birthDate")}</label>
                <input type="date" id="birthDate" name="user.attributes.birthDate" class="${properties.kcInputClass!}"
                       value="${(register.formData['user.attributes.birthDate']!'')}" required />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <label for="educationLevel" class="${properties.kcLabelClass!}">${msg("educationLevel")}</label>
                <select id="educationLevel" name="user.attributes.educationLevel" class="${properties.kcInputClass!}" required>
                    <option value="fundamental">Fundamental</option>
                    <option value="medio">Medio</option>
                    <option value="graduacao">Graduacao</option>
                    <option value="posGraduacao">Pos-graduacao</option>
                    <option value="outro">Outro</option>
                </select>
            </div>

            <div class="${properties.kcFormGroupClass!} profile-fixed">
                <label for="profile" class="${properties.kcLabelClass!}">${msg("profile")}</label>
                <input type="text" id="profile" class="${properties.kcInputClass!}" value="${msg("profileEstudante")}" readonly />
                <input type="hidden" name="user.attributes.profile" value="estudante" />
            </div>

            <div class="${properties.kcFormGroupClass!}">
                <input type="submit" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!}" value="${msg("doRegister")}" />
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
