import { defineStore}  from 'pinia';

export const useAccountStore = defineStore('account', {
    state: () => ({
        checked: false,
        loggedIn: false,
        member: null,
        accessToken: '',
    }),
    actions: {
        setChecked(val){
            this.checked = val;
        },
        setLoggedIn(val, memberData = null){
            this.loggedIn  = val;

            if(val){
                if(memberData){
                    const {accessToken, ...userRestData} = memberData;
                    this.member = userRestData;
                    localStorage.setItem('member', JSON.stringify(userRestData));
                }
            }else{
                this.member = null;
                localStorage.removeItem('member');
            }
        },
        setAccessToken(val){
            this.accessToken = val;

            if(val){
                localStorage.setItem('accessToken', val);
            }else{
                localStorage.removeItem('accessToken');
            }
        },
        
        //로그아웃
        clearAccount(){
            this.checked = false;
            this.loggedIn = false;
            this.member = null,
            this.accessToken = '';

            localStorage.removeItem('accessToken');
            localStorage.removeItem('member');
        },
    },
    persist: true,
});
