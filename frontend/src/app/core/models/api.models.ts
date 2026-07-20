export type ProfileType = 'estudante' | 'admin';

export type EducationLevel = 'fundamental' | 'medio' | 'graduacao' | 'posGraduacao' | 'outro';

export type LoginChangeType = 'email' | 'password' | 'emailAndPassword';

export type LoginChangeStatus = 'pending' | 'approved' | 'rejected' | 'completed';

export interface UserResponse {
  id: string;
  keycloakId: string;
  name: string;
  email: string;
  cpf: string;
  birthDate: string;
  educationLevel: EducationLevel;
  profile: ProfileType;
  createdAt: string;
  updatedAt: string;
  disabled: boolean;
}

export interface UserBootstrapRequest {
  name: string;
  cpf: string;
  birthDate: string;
  educationLevel: EducationLevel;
}

export interface AdminCreateUserRequest {
  name: string;
  email: string;
  cpf: string;
  birthDate: string;
  educationLevel: EducationLevel;
  profile: ProfileType;
  password: string;
}

export interface AdminUpdateUserRequest {
  name?: string;
  cpf?: string;
  birthDate?: string;
  educationLevel?: EducationLevel;
  profile?: ProfileType;
}

export interface LoginChangeCreateRequest {
  changeType: LoginChangeType;
  proposedEmail?: string;
}

export interface LoginChangeCompleteRequest {
  cpf: string;
  newEmail?: string;
  newPassword?: string;
}

export interface LoginChangeResponse {
  id: string;
  requesterUserId: string;
  changeType: LoginChangeType;
  status: LoginChangeStatus;
  proposedEmail?: string;
  reviewedByUserId?: string;
  reviewedAt?: string;
  completedAt?: string;
  createdAt: string;
  updatedAt: string;
  rejectionReason?: string;
}

export const EDUCATION_LEVEL_LABELS: Record<EducationLevel, string> = {
  fundamental: 'Fundamental',
  medio: 'Médio',
  graduacao: 'Graduação',
  posGraduacao: 'Pós-graduação',
  outro: 'Outro'
};

export const PROFILE_LABELS: Record<ProfileType, string> = {
  estudante: 'Estudante',
  admin: 'Admin'
};

export const LOGIN_CHANGE_TYPE_LABELS: Record<LoginChangeType, string> = {
  email: 'E-mail',
  password: 'Senha',
  emailAndPassword: 'E-mail e senha'
};

export const LOGIN_CHANGE_STATUS_LABELS: Record<LoginChangeStatus, string> = {
  pending: 'Pendente',
  approved: 'Aprovada',
  rejected: 'Rejeitada',
  completed: 'Concluída'
};
