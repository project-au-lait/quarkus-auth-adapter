export interface UserModel {
  userName: string;
  password: string;
  email: string;
}

export class UserFactory {
  static createUser1() {
    return {
      userName: 'user-1',
      password: 'user-1',
      email: 'user-1@sqk.dev'
    } as UserModel;
  }
}
