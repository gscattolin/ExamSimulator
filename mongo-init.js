db = db.getSiblingDB('examsim')
db.createUser(
    {
        user: 'mongoadmin',
        pwd: 'mongoadmin',
        roles: [
            {
                role: 'readWrite',
                db: 'examsim',
            }
        ]
    }
);